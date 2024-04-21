package pl.smarthouse.externallightsmodule.properties;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Esp32ModuleProperties {

  // Module specific
  public static final String FIRMWARE = "20240421.15";
  public static final String VERSION = "20240421.15";
  public static final String MAC_ADDRESS = "24:DC:C3:9F:EE:DC";
  public static final String MODULE_TYPE = "EXTERNAL_LIGHTS";
}
