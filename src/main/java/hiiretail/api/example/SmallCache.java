package hiiretail.api.example;

import static io.vertx.core.Future.succeededFuture;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.impl.LRUCache;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a cache to demonstrate SPI
 *
 * @see Cache
 * @author thced
 */
public class SmallCache implements Cache<JsonObject> {

  private static final Logger log = LoggerFactory.getLogger(SmallCache.class);

  private final AtomicInteger counter = new AtomicInteger(1);
  private final Map<Integer, JsonObject> cache;

  public SmallCache() {
    this.cache = createCache();
  }

  Map<Integer, JsonObject> createCache() {
    return new LRUCache<>(10000);
  }

  @Override
  public Future<JsonArray> retrieve() {
    return succeededFuture(
        cache.entrySet().stream()
            .collect(
                JsonArray::new, (array, entry) -> array.add(entry.getValue()), JsonArray::addAll));
  }

  @Override
  public Future<Void> add(JsonObject entity) {
    cache.put(counter.getAndIncrement(), entity);
    return succeededFuture()
        .onSuccess(ignore -> log.info("Stored entity={}", entity))
        .mapEmpty();
  }
}
