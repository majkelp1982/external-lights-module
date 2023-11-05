package pl.smarthouse.externallightsmodule.chain;

import static pl.smarthouse.externallightsmodule.properties.ActorProperties.*;

import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.externallightsmodule.service.ExternalLightsModuleParamsService;
import pl.smarthouse.externallightsmodule.service.ExternalLightsModuleService;
import pl.smarthouse.externallightsmodule.service.WeatherModuleService;
import pl.smarthouse.externallightsmodule.utils.TimeRangeUtils;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.externallights.core.LightZoneDto;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RbdDimmer;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerCommandSet;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerCommandType;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;

@Service
@Slf4j
public class DimmerChain {

  private final RbdDimmer entranceDimmer;
  private final RbdDimmer drivewayDimmer;
  private final RbdDimmer carportDimmer;
  private final RbdDimmer gardenDimmer;
  private final ExternalLightsModuleService externalLightsModuleService;
  private final ExternalLightsModuleParamsService externalLightsModuleParamsService;
  private final WeatherModuleService weatherModuleService;
  ExternalLightsModuleParamsDto serviceParams;
  int currentLightIntense = 0;

  public DimmerChain(
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final ExternalLightsModuleService externalLightsModuleService,
      @Autowired final ExternalLightsModuleParamsService externalLightsModuleParamsService,
      @Autowired final WeatherModuleService weatherModuleService,
      @Autowired final ChainService chainService) {

    entranceDimmer = getDimmers(esp32ModuleConfig, ENTRANCE_NAME);
    drivewayDimmer = getDimmers(esp32ModuleConfig, DRIVEWAY_NAME);
    carportDimmer = getDimmers(esp32ModuleConfig, CARPORT_NAME);
    gardenDimmer = getDimmers(esp32ModuleConfig, GARDEN_NAME);

    this.externalLightsModuleService = externalLightsModuleService;
    this.externalLightsModuleParamsService = externalLightsModuleParamsService;
    this.weatherModuleService = weatherModuleService;
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Dimmers");
    // Get service params
    chain.addStep(getServiceParams());
    // Wait 10 seconds and send calculated power if differ from goal power to all dimmers
    chain.addStep(wait10secondsAndSendCalculatedPowerToDimmers());
    // Wait until command successful and set command to NO_ACTION for all dimmer
    chain.addStep(waitForResponseAndSetNoAction());
    return chain;
  }

  private Step getServiceParams() {

    return Step.builder()
        .stepDescription("Get service params")
        .conditionDescription("Waiting 2 seconds")
        .condition(PredicateUtils.delaySeconds(2))
        .action(getServiceParamsExecution())
        .build();
  }

  private Runnable getServiceParamsExecution() {
    return () -> {
      serviceParams = externalLightsModuleParamsService.getParams().block();
      if (weatherModuleService.getWeatherMetadata() != null) {
        currentLightIntense =
            weatherModuleService.getWeatherMetadata().getLightIntense().getPinValue();
      }
      saveResponses();
    };
  }

  private Step wait10secondsAndSendCalculatedPowerToDimmers() {

    return Step.builder()
        .stepDescription("Calculate power and send if goal power different")
        .conditionDescription("no condition")
        .condition(
            (actor) -> {
              return true;
            })
        .action(calculatePowerAndSend())
        .build();
  }

  private Runnable calculatePowerAndSend() {
    return () -> {
      calculatePowerAndSendIfNeeded(entranceDimmer, serviceParams.getDriveway());
      calculatePowerAndSendIfNeeded(drivewayDimmer, serviceParams.getDriveway());
      calculatePowerAndSendIfNeeded(carportDimmer, serviceParams.getCarport());
      calculatePowerAndSendIfNeeded(gardenDimmer, serviceParams.getGarden());
    };
  }

  private Step waitForResponseAndSetNoAction() {
    return Step.builder()
        .stepDescription("Set all dimmers command to NO_ACTION")
        .conditionDescription("Wait until send command successful")
        .condition(
            isPowerSet(entranceDimmer)
                .and(
                    isPowerSet(drivewayDimmer)
                        .and(isPowerSet(carportDimmer).and(isPowerSet(gardenDimmer)))))
        .action(setNoAction())
        .build();
  }

  private Runnable setNoAction() {
    return () -> {
      saveResponses();
      entranceDimmer.getCommandSet().setCommandType(RdbDimmerCommandType.NO_ACTION);
      drivewayDimmer.getCommandSet().setCommandType(RdbDimmerCommandType.NO_ACTION);
      carportDimmer.getCommandSet().setCommandType(RdbDimmerCommandType.NO_ACTION);
      gardenDimmer.getCommandSet().setCommandType(RdbDimmerCommandType.NO_ACTION);
    };
  }

  private Predicate<Step> isPowerSet(final RbdDimmer rbdDimmer) {
    return step -> {
      final RdbDimmerResponse rdbDimmerResponse = rbdDimmer.getResponse();
      return (rdbDimmerResponse == null)
          ? false
          : rdbDimmerResponse.getGoalPower() == rdbDimmerResponse.getPower();
    };
  }

  private RbdDimmer getDimmers(final Esp32ModuleConfig esp32ModuleConfig, final String dimmerName) {
    return (RbdDimmer) esp32ModuleConfig.getConfiguration().getActorMap().getActor(dimmerName);
  }

  private void calculatePowerAndSendIfNeeded(
      final RbdDimmer rbdDimmer, final LightZoneDto lightZoneDto) {
    final RdbDimmerResponse dimmerResponse = rbdDimmer.getResponse();
    if (dimmerResponse == null) {
      rbdDimmer.getCommandSet().setCommandType(RdbDimmerCommandType.READ);
      return;
    }
    final int currentPower = dimmerResponse.getPower();
    int goalPower = 0;
    if (lightZoneDto.isForceMin()) {
      goalPower = 0;
    } else if (lightZoneDto.isForceMax()) {
      goalPower = lightZoneDto.getMaxPower();
    } else {
      goalPower = calculateBaseOnLightIntense(lightZoneDto.getStandByPower());
    }

    if (currentPower == goalPower) {
      rbdDimmer.setCommandSet(new RdbDimmerCommandSet(RdbDimmerCommandType.NO_ACTION));
    } else {
      log.info(
          "Dimmer: {}, sending new power: {}, current: {}",
          rbdDimmer.getName(),
          goalPower,
          currentPower);
      rbdDimmer.getResponse().setGoalPower(goalPower);
      rbdDimmer.setCommandSet(
          new RdbDimmerCommandSet(RdbDimmerCommandType.POWER, String.valueOf(goalPower)));
    }
  }

  private int calculateBaseOnLightIntense(final int standbyPower) {
    if (!TimeRangeUtils.inTimeRange(Set.of(serviceParams.getEnableTimeRange()))) {
      return 0;
    }
    if (currentLightIntense > serviceParams.getLightIntenseThreshold()) {
      return 0;
    }
    return ((serviceParams.getLightIntenseThreshold() - currentLightIntense)
        * standbyPower
        / serviceParams.getLightIntenseThreshold());
  }

  private void saveResponses() {
    externalLightsModuleService.setEntranceDimmerResponse(entranceDimmer.getResponse());
    externalLightsModuleService.setDrivewayDimmerResponse(drivewayDimmer.getResponse());
    externalLightsModuleService.setCarportDimmerResponse(carportDimmer.getResponse());
    externalLightsModuleService.setGardenDimmerResponse(gardenDimmer.getResponse());
  }
}
