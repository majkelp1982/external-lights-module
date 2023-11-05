package pl.smarthouse.externallightsmodule.model.dao;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import pl.smarthouse.sharedobjects.dao.ModuleDao;

@Data
@SuperBuilder
public class ExternalLightsModuleDao extends ModuleDao {
  private LightZoneDao entrance;
  private LightZoneDao driveway;
  private LightZoneDao carport;
  private LightZoneDao garden;
}
