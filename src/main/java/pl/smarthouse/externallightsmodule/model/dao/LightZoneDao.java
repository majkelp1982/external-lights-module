package pl.smarthouse.externallightsmodule.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LightZoneDao {
  int maxPower;
  int standByPower;
  private boolean forceMax;
  private boolean forceMin;
}
