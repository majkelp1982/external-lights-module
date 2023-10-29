package pl.smarthouse.externallightsmodule.configurations;

import static pl.smarthouse.externallightsmodule.properties.ActorProperties.*;
import static pl.smarthouse.externallightsmodule.properties.Esp32ModuleProperties.*;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.rdbDimmer.RbdDimmer;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

@Configuration
@RequiredArgsConstructor
@Getter
public class Esp32ModuleConfig {
  private final ModuleService moduleService;
  private final ManagerService managerService;

  // Module specific
  private pl.smarthouse.smartmodule.model.configuration.Configuration configuration;

  @PostConstruct
  public void postConstruct() {
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            MODULE_TYPE, FIRMWARE, VERSION, MAC_ADDRESS, createActors());
    moduleService.setConfiguration(configuration);
    managerService.setConfiguration(configuration);
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();
    actorMap.putActor(new RbdDimmer(ENTRANCE_NAME, ENTRANCE_PIN, CARPORT_PIN));
    actorMap.putActor(new RbdDimmer(DRIVEWAY_NAME, DRIVEWAY_PIN, CARPORT_PIN));
    actorMap.putActor(new RbdDimmer(CARPORT_NAME, CARPORT_PIN, CARPORT_PIN));
    actorMap.putActor(new RbdDimmer(GARDEN_NAME, GARDEN_PIN, CARPORT_PIN));
    return actorMap;
  }
}
