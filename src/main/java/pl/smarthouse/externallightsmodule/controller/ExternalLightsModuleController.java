package pl.smarthouse.externallightsmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.externallightsmodule.service.ExternalLightsModuleParamsService;
import pl.smarthouse.externallightsmodule.service.ExternalLightsModuleService;
import pl.smarthouse.externallightsmodule.service.WeatherModuleService;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.weather.WeatherModuleDto;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class ExternalLightsModuleController {
  private final ExternalLightsModuleService externalLightsModuleService;
  private final ExternalLightsModuleParamsService externalLightsModuleParamsService;
  private final WeatherModuleService weatherModuleService;

  @GetMapping("/lights")
  public Mono<ExternalLightsModuleDto> getExternalLightsModule() {
    return Mono.just(externalLightsModuleService.getModule());
  }

  @PatchMapping("/lights/{zonename}")
  public Mono<ExternalLightsModuleDto> setForceMaxMin(
      @PathVariable("zonename") final String zoneName,
      @RequestParam final boolean forceMax,
      @RequestParam final boolean forceMin) {
    return Mono.just(externalLightsModuleService.setForceMaxMin(zoneName, forceMax, forceMin));
  }

  @GetMapping("/weather/metadata")
  public Mono<WeatherModuleDto> getWeatherMetadata() {
    return Mono.just(weatherModuleService.getWeatherMetadata());
  }

  @PostMapping("/params")
  public Mono<ExternalLightsModuleParamsDto> saveParams(
      @RequestBody final ExternalLightsModuleParamsDto externalLightsModuleParamsDto) {
    return externalLightsModuleParamsService.saveParams(externalLightsModuleParamsDto);
  }

  @GetMapping("/params")
  public Mono<ExternalLightsModuleParamsDto> getParams() {
    return externalLightsModuleParamsService.getParams();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Mono<String> exceptionHandler(final Exception exception) {
    return Mono.just(exception.getMessage());
  }
}
