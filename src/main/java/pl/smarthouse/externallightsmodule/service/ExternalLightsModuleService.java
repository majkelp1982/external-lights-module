package pl.smarthouse.externallightsmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.ExternalLightsModuleConfiguration;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
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
}
