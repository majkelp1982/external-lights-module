package pl.smarthouse.externallightsmodule.model.dao;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;

@Data
@SuperBuilder
public class ExternalLightsModuleDao extends ModuleDao {
  private RdbDimmerResponse entrance;
  private RdbDimmerResponse driveway;
  private RdbDimmerResponse carport;
  private RdbDimmerResponse garden;
}
