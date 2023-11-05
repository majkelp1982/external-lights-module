package pl.smarthouse.externallightsmodule.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.model.dao.ExternalLightsModuleParamsDao;
import pl.smarthouse.externallightsmodule.repository.ParamsRepository;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleParamsDto;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalLightsModuleParamsService {
  private final ParamsRepository paramsRepository;
  private final ExternalLightsModuleService externalLightsModuleService;
  private final ModelMapper modelMapper = new ModelMapper();

  public Mono<ExternalLightsModuleParamsDto> saveParams(
      final ExternalLightsModuleParamsDto externalLightsModuleParamsDto) {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository.saveParams(
                    modelMapper.map(
                        externalLightsModuleParamsDto, ExternalLightsModuleParamsDao.class),
                    paramTableName))
        .map(
            externalLightsModuleParamsDao ->
                modelMapper.map(
                    externalLightsModuleParamsDao, ExternalLightsModuleParamsDto.class));
  }

  private Mono<String> getParamTableName() {
    return Mono.just(externalLightsModuleService.getModuleName())
        .map(moduleName -> moduleName.toLowerCase() + "_settings");
  }

  public Mono<ExternalLightsModuleParamsDto> getParams() {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository
                    .getParams(paramTableName)
                    .doOnNext(
                        externalLightsModuleParamsDao ->
                            log.debug(
                                "Successfully retrieve params: {}", externalLightsModuleParamsDao))
                    .map(
                        externalLightsModuleParamsDao ->
                            modelMapper.map(
                                externalLightsModuleParamsDao, ExternalLightsModuleParamsDto.class))
                    .onErrorResume(
                        NoSuchElementException.class,
                        throwable -> {
                          log.warn("No params found for: {}", paramTableName);
                          return Mono.empty();
                        })
                    .doOnError(
                        throwable ->
                            log.error(
                                "Error on get params. Error message: {}, Error: {}",
                                throwable.getMessage(),
                                throwable))
                    .doOnSubscribe(
                        subscription ->
                            log.debug("Get module params from collection: {}", paramTableName)));
  }
}
