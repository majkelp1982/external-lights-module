package pl.smarthouse.externallightsmodule.chain;

import static pl.smarthouse.externallightsmodule.properties.ActorProperties.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.externallightsmodule.model.dao.LightZoneDao;
import pl.smarthouse.externallightsmodule.service.ExternalLightsModuleParamsService;
import pl.smarthouse.externallightsmodule.service.ExternalLightsModuleService;
import pl.smarthouse.externallightsmodule.service.WeatherModuleService;
import pl.smarthouse.externallightsmodule.utils.TimeRangeUtils;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.externallights.core.LightZoneParamDto;
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
    chain.addStep(waitForParamsAndSendCalculatedPowerToDimmers());
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
      externalLightsModuleParamsService
          .getParams()
          .doOnNext(externalLightsModuleParamsDto -> serviceParams = externalLightsModuleParamsDto)
          .subscribe();

      if (weatherModuleService.getWeatherMetadata() != null) {
        currentLightIntense =
            weatherModuleService.getWeatherMetadata().getLightIntense().getPinValue();
      }
      saveResponses();
    };
  }

  private Step waitForParamsAndSendCalculatedPowerToDimmers() {

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
      if (serviceParams == null) {
        return;
      }
      calculatePowerAndSendIfNeeded(
          entranceDimmer,
          externalLightsModuleService.getEntranceLightZone(),
          serviceParams.getDriveway());
      calculatePowerAndSendIfNeeded(
          drivewayDimmer,
          externalLightsModuleService.getDrivewayLightZone(),
          serviceParams.getDriveway());
      calculatePowerAndSendIfNeeded(
          carportDimmer,
          externalLightsModuleService.getCarportLightZone(),
          serviceParams.getCarport());
      calculatePowerAndSendIfNeeded(
          gardenDimmer,
          externalLightsModuleService.getGardenLightZone(),
          serviceParams.getGarden());
    };
  }

  private Step waitForResponseAndSetNoAction() {
    return Step.builder()
        .stepDescription("Set all dimmers command to NO_ACTION")
        .conditionDescription("Wait until send command successful")
        .condition(
            isPowerSetAndResponseNotOlderThen30Sec(entranceDimmer)
                .and(
                    isPowerSetAndResponseNotOlderThen30Sec(drivewayDimmer)
                        .and(
                            isPowerSetAndResponseNotOlderThen30Sec(carportDimmer)
                                .and(isPowerSetAndResponseNotOlderThen30Sec(gardenDimmer)))))
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

  private Predicate<Step> isPowerSetAndResponseNotOlderThen30Sec(final RbdDimmer rbdDimmer) {
    return step -> {
      final RdbDimmerResponse rdbDimmerResponse = rbdDimmer.getResponse();
      return (rdbDimmerResponse == null)
          ? false
          : rdbDimmerResponse.getGoalPower() == rdbDimmerResponse.getPower()
              && LocalDateTime.now()
                  .isBefore(rdbDimmerResponse.getResponseUpdate().plusSeconds(30));
    };
  }

  private RbdDimmer getDimmers(final Esp32ModuleConfig esp32ModuleConfig, final String dimmerName) {
    return (RbdDimmer) esp32ModuleConfig.getConfiguration().getActorMap().getActor(dimmerName);
  }

  private void calculatePowerAndSendIfNeeded(
      final RbdDimmer rbdDimmer,
      final LightZoneDao lightZoneDao,
      final LightZoneParamDto lightZoneParamDto) {
    final RdbDimmerResponse dimmerResponse = rbdDimmer.getResponse();
    if (dimmerResponse == null) {
      rbdDimmer.getCommandSet().setCommandType(RdbDimmerCommandType.READ);
      return;
    }
    final int currentPower = dimmerResponse.getPower();
    int goalPower = 0;
    if (lightZoneDao.isForceMin()) {
      goalPower = 0;
    } else if (lightZoneDao.isForceMax()) {
      goalPower = lightZoneParamDto.getMaxPower();
    } else {
      goalPower = calculateBaseOnLightIntense(lightZoneParamDto.getStandByPower());
    }

    if (currentPower == goalPower
        && LocalDateTime.now().isBefore(dimmerResponse.getResponseUpdate().plusSeconds(30))) {
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
    if (entranceDimmer.getResponse() != null) {
      externalLightsModuleService.setEntranceDimmerResponse(entranceDimmer.getResponse());
    }
    if (drivewayDimmer.getResponse() != null) {
      externalLightsModuleService.setDrivewayDimmerResponse(drivewayDimmer.getResponse());
    }
    if (carportDimmer.getResponse() != null) {
      externalLightsModuleService.setCarportDimmerResponse(carportDimmer.getResponse());
    }
    if (gardenDimmer.getResponse() != null) {
      externalLightsModuleService.setGardenDimmerResponse(gardenDimmer.getResponse());
    }
  }
}
