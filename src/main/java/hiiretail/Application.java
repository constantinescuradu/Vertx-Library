package hiiretail;

import static io.vertx.core.impl.cpu.CpuCoreSensor.availableProcessors;
import static java.util.Arrays.asList;

import hiiretail.api.ApiRouter;
import hiiretail.api.example.CacheVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The 'Application' is the first verticle to be deployed by Vert.x, and its responsibility is to
 * fetch configuration needed to deploy all the following verticles correctly.
 *
 * @author thced
 */
public class Application extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  /**
   * Controls the level of parallelism
   *
   * <p><b>Note!</b> Not all applications need [n] number of instances, benchmark!
   */
  private static final Integer INSTANCES = availableProcessors();

  @Override
  public void start(Promise<Void> startPromise) {
    long start = System.nanoTime();
    ConfigurationRetriever.configuration(vertx)
        .compose(this::deploy)
        .onSuccess(v -> logStart(start))
        .onComplete(startPromise);
  }

  private Future<Void> deploy(JsonObject config) {
    DeploymentOptions options = new DeploymentOptions().setConfig(config).setInstances(INSTANCES);
    DeploymentOptions singleton = new DeploymentOptions(options).setInstances(1);

    return CompositeFuture.all(
            asList(
                vertx.deployVerticle(CacheVerticle::new, singleton),
                vertx.deployVerticle(ApiRouter::new, options)
                // .. potentially more verticles here..
                ))
        .onSuccess(ignore -> log.info("All verticles successfully deployed."))
        .onFailure(throwable -> log.error("Not all verticles could be deployed", throwable))
        .mapEmpty();
  }

  private void logStart(long since) {
    log.info("Application started in {} ms.", convertNanosToMillis(since));
  }

  private long convertNanosToMillis(long since) {
    return TimeUnit.MILLISECONDS.convert(System.nanoTime() - since, TimeUnit.NANOSECONDS);
  }
}
