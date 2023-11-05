package pl.smarthouse.externallightsmodule.service;

import static pl.smarthouse.externallightsmodule.properties.ActorProperties.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.ExternalLightsModuleConfiguration;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RbdDimmer;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerCommandSet;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerCommandType;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class ExternalLightsModuleService {
  private final ExternalLightsModuleConfiguration externalLightsModuleConfiguration;
  private final ModelMapper modelMapper = new ModelMapper();

  public ExternalLightsModuleDto getModule() {
    return modelMapper.map(
        externalLightsModuleConfiguration.getExternalLightsModuleDao(),
        ExternalLightsModuleDto.class);
  }

  public String getModuleName() {
    return externalLightsModuleConfiguration.getExternalLightsModuleDao().getModuleName();
  }

  public void setEntranceDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration.getExternalLightsModuleDao().setEntrance(rdbDimmerResponse);
  }

  public void setDrivewayDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration.getExternalLightsModuleDao().setDriveway(rdbDimmerResponse);
  }

  public void setCarportDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration.getExternalLightsModuleDao().setCarport(rdbDimmerResponse);
  }

  public void setGardenDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration.getExternalLightsModuleDao().setGarden(rdbDimmerResponse);
  }

  @Scheduled(fixedDelay = 10000)
  private void temporary() {
    final List<RbdDimmer> rbdDimmers = new ArrayList<>();
    rbdDimmers.add(
        (RbdDimmer)
            externalLightsModuleConfiguration
                .getEsp32ModuleConfig()
                .getConfiguration()
                .getActorMap()
                .getActor(ENTRANCE_NAME));
    rbdDimmers.add(
        (RbdDimmer)
            externalLightsModuleConfiguration
                .getEsp32ModuleConfig()
                .getConfiguration()
                .getActorMap()
                .getActor(DRIVEWAY_NAME));
    rbdDimmers.add(
        (RbdDimmer)
            externalLightsModuleConfiguration
                .getEsp32ModuleConfig()
                .getConfiguration()
                .getActorMap()
                .getActor(CARPORT_NAME));
    rbdDimmers.add(
        (RbdDimmer)
            externalLightsModuleConfiguration
                .getEsp32ModuleConfig()
                .getConfiguration()
                .getActorMap()
                .getActor(GARDEN_NAME));

    final LocalTime from = LocalTime.of(16, 30);
    final LocalTime to = LocalTime.of(23, 30);

    final int powerMax = 30;
    int resultPower = 0;
    if (LocalTime.now().isAfter(from)) {
      resultPower = powerMax;
    }
    if (LocalTime.now().isAfter(to)) {
      resultPower = 0;
    }
    final RdbDimmerCommandSet rdbDimmerCommandSet =
        new RdbDimmerCommandSet(RdbDimmerCommandType.POWER, String.valueOf(resultPower));
    log.info("Sending command set to all dimmers: {}", rdbDimmerCommandSet);
    rbdDimmers.forEach(rbdDimmer -> rbdDimmer.setCommandSet(rdbDimmerCommandSet));
  }
}
