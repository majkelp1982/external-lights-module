package pl.smarthouse.externallightsmodule.model.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;

@Data
@NoArgsConstructor
public class ExternalLightsModuleParamsDao {
  TimeRange enableTimeRange;
  private LightZoneDao entrance;
  private LightZoneDao driveway;
  private LightZoneDao carport;
  private LightZoneDao garden;
  private int lightIntenseThreshold;
}
