package pl.smarthouse.externallightsmodule.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RdbDimmerResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LightZoneDao {
  RdbDimmerResponse rdbDimmerResponse;
  private boolean forceMax;
  private boolean forceMin;
}
