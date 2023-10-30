package pl.smarthouse.externallightsmodule.configurations;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.externallightsmodule.model.dao.ExternalLightsModuleDao;
import pl.smarthouse.externallightsmodule.properties.Esp32ModuleProperties;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;
import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.properties.defaults.RdbDimmerDefaultProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;
import pl.smarthouse.smartmonitoring.service.MonitoringService;

@Configuration
@RequiredArgsConstructor
@Getter
@Slf4j
public class ExternalLightsModuleConfiguration {
  private final CompareProcessor compareProcessor;
  private final MonitoringService monitoringService;
  private final Esp32ModuleConfig esp32ModuleConfig;
  private final Esp32ModuleProperties esp32ModuleProperties;
  private ExternalLightsModuleDao externalLightsModuleDao;

  @PostConstruct
  void postConstruct() {
    final RdbDimmerResponse entrance = new RdbDimmerResponse();
    entrance.setMode("NO_READY");
    final RdbDimmerResponse driveway = new RdbDimmerResponse();
    driveway.setMode("NO_READY");
    final RdbDimmerResponse carport = new RdbDimmerResponse();
    carport.setMode("NO_READY");
    final RdbDimmerResponse garden = new RdbDimmerResponse();
    garden.setMode("NO_READY");
    externalLightsModuleDao =
        ExternalLightsModuleDao.builder()
            .entrance(entrance)
            .driveway(driveway)
            .carport(carport)
            .garden(garden)
            .moduleName(Esp32ModuleProperties.MODULE_TYPE)
            .build();
    monitoringService.setModuleDaoObject(externalLightsModuleDao);
    setCompareProperties();
  }

  private void setCompareProperties() {
    compareProcessor.addMap("error", BooleanCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "errorPendingAcknowledge", BooleanCompareProperties.builder().saveEnabled(true).build());
    RdbDimmerDefaultProperties.setDefaultProperties(compareProcessor, "entrance");
    RdbDimmerDefaultProperties.setDefaultProperties(compareProcessor, "driveway");
    RdbDimmerDefaultProperties.setDefaultProperties(compareProcessor, "carport");
    RdbDimmerDefaultProperties.setDefaultProperties(compareProcessor, "garden");
  }
}
