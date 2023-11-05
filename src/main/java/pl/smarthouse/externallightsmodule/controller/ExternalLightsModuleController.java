package pl.smarthouse.externallightsmodule.controller;

import lombok.RequiredArgsConstructor;
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
}
