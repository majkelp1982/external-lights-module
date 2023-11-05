package pl.smarthouse.externallightsmodule.model.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;

@Data
@NoArgsConstructor
public class ExternalLightsModuleParamsDao {
  TimeRange enableTimeRange;
  private LightZoneParamDao entrance;
  private LightZoneParamDao driveway;
  private LightZoneParamDao carport;
  private LightZoneParamDao garden;
  private int lightIntenseThreshold;
}
