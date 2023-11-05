package pl.smarthouse.externallightsmodule.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.smarthouse.externallightsmodule.model.dao.ExternalLightsModuleParamsDao;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ParamsRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<ExternalLightsModuleParamsDao> saveParams(
      final ExternalLightsModuleParamsDao externalLightsModuleParamsDao,
      final String paramTableName) {
    return reactiveMongoTemplate
        .remove(new Query(), ExternalLightsModuleParamsDao.class, paramTableName)
        .then(reactiveMongoTemplate.save(externalLightsModuleParamsDao, paramTableName));
  }

  public Mono<ExternalLightsModuleParamsDao> getParams(final String paramTableName) {
    return reactiveMongoTemplate
        .findAll(ExternalLightsModuleParamsDao.class, paramTableName)
        .last()
        .cache(Duration.ofMinutes(1));
  }
}
