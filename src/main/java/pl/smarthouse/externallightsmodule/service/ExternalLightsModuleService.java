package pl.smarthouse.externallightsmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.ExternalLightsModuleConfiguration;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
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
}
