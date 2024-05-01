package pl.smarthouse.externallightsmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.configurations.ExternalLightsModuleConfiguration;
import pl.smarthouse.externallightsmodule.exceptions.BadZoneNameException;
import pl.smarthouse.externallightsmodule.model.dao.LightZoneDao;
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

  public ExternalLightsModuleDto setForceMaxMin(
      final String zoneName, final boolean forceMax, final boolean forceMin) {
    final LightZoneDao lightZoneDao;
    switch (zoneName) {
      case "entrance" -> lightZoneDao =
          externalLightsModuleConfiguration.getExternalLightsModuleDao().getEntrance();
      case "driveway" -> lightZoneDao =
          externalLightsModuleConfiguration.getExternalLightsModuleDao().getDriveway();
      case "carport" -> lightZoneDao =
          externalLightsModuleConfiguration.getExternalLightsModuleDao().getCarport();
      case "garden" -> lightZoneDao =
          externalLightsModuleConfiguration.getExternalLightsModuleDao().getGarden();
      default -> throw new BadZoneNameException(
          String.format("Provided zone name: %s, don't match any of known zones", zoneName));
    }

    lightZoneDao.setForceMax(forceMax);
    lightZoneDao.setForceMin(forceMin);

    return modelMapper.map(
        externalLightsModuleConfiguration.getExternalLightsModuleDao(),
        ExternalLightsModuleDto.class);
  }

  public String getModuleName() {
    return externalLightsModuleConfiguration.getExternalLightsModuleDao().getType();
  }

  public void setEntranceDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration
        .getExternalLightsModuleDao()
        .getEntrance()
        .setRdbDimmerResponse(rdbDimmerResponse);
  }

  public void setDrivewayDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration
        .getExternalLightsModuleDao()
        .getDriveway()
        .setRdbDimmerResponse(rdbDimmerResponse);
  }

  public void setCarportDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration
        .getExternalLightsModuleDao()
        .getCarport()
        .setRdbDimmerResponse(rdbDimmerResponse);
  }

  public void setGardenDimmerResponse(final RdbDimmerResponse rdbDimmerResponse) {
    externalLightsModuleConfiguration
        .getExternalLightsModuleDao()
        .getGarden()
        .setRdbDimmerResponse(rdbDimmerResponse);
  }

  public LightZoneDao getEntranceLightZone() {
    return externalLightsModuleConfiguration.getExternalLightsModuleDao().getEntrance();
  }

  public LightZoneDao getDrivewayLightZone() {
    return externalLightsModuleConfiguration.getExternalLightsModuleDao().getDriveway();
  }

  public LightZoneDao getCarportLightZone() {
    return externalLightsModuleConfiguration.getExternalLightsModuleDao().getCarport();
  }

  public LightZoneDao getGardenLightZone() {
    return externalLightsModuleConfiguration.getExternalLightsModuleDao().getGarden();
  }
}
