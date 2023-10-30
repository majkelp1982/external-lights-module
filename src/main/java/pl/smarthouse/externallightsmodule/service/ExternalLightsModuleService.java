package pl.smarthouse.externallightsmodule.service;

import static pl.smarthouse.externallightsmodule.properties.ActorProperties.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.ExternalLightsModuleConfiguration;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RbdDimmer;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerCommandSet;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerCommandType;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class ExternalLightsModuleService {
  private final ExternalLightsModuleConfiguration externalLightsModuleConfiguration;

  public final Mono<RdbDimmerResponse> setEntranceDimmerResponse(
      final RdbDimmerResponse rdbDimmerResponse) {
    return Mono.just(externalLightsModuleConfiguration.getExternalLightsModuleDao())
        .doOnNext(externalLightsModuleDao -> externalLightsModuleDao.setEntrance(rdbDimmerResponse))
        .thenReturn(rdbDimmerResponse);
  }

  public final Mono<RdbDimmerResponse> setDrivewayDimmerResponse(
      final RdbDimmerResponse rdbDimmerResponse) {
    return Mono.just(externalLightsModuleConfiguration.getExternalLightsModuleDao())
        .doOnNext(externalLightsModuleDao -> externalLightsModuleDao.setDriveway(rdbDimmerResponse))
        .thenReturn(rdbDimmerResponse);
  }

  public final Mono<RdbDimmerResponse> setCarportDimmerResponse(
      final RdbDimmerResponse rdbDimmerResponse) {
    return Mono.just(externalLightsModuleConfiguration.getExternalLightsModuleDao())
        .doOnNext(externalLightsModuleDao -> externalLightsModuleDao.setCarport(rdbDimmerResponse))
        .thenReturn(rdbDimmerResponse);
  }

  public final Mono<RdbDimmerResponse> setGradenDimmerResponse(
      final RdbDimmerResponse rdbDimmerResponse) {
    return Mono.just(externalLightsModuleConfiguration.getExternalLightsModuleDao())
        .doOnNext(externalLightsModuleDao -> externalLightsModuleDao.setGarden(rdbDimmerResponse))
        .thenReturn(rdbDimmerResponse);
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
