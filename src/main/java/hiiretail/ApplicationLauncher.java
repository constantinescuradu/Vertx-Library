package hiiretail;

import static java.lang.System.setProperty;

import hiiretail.eventbus.InboundCorrelationId;
import hiiretail.eventbus.OutboundCorrelationId;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import java.util.Objects;

/**
 * Launcher for Vert.x application.
 *
 * <p>Contains possible hooks for customizing the start/stop of the application.
 *
 * @author thced
 */
public class ApplicationLauncher extends Launcher {

  /*
   Note: All configuration constants should be put where they are used. Every
   verticle should present all its related configurations as constants.
  */
  private static final String HAZELCAST_HEALTH_MONITORING_LEVEL =
      "hazelcast.health.monitoring.level";
  private static final String DEFAULT_HAZELCAST_LEVEL = "SILENT";
  private static final String HAZELCAST_LOG_TYPE = "hazelcast.logging.type";

  /** Start up the Vert.x application. */
  public static void main(String[] args) {
    new ApplicationLauncher().dispatch(args);
  }

  @Override
  public void beforeStartingVertx(VertxOptions options) {
    super.beforeStartingVertx(options);

    configureClustering(options);
    configureMetricsSupport(options);
  }

  @Override
  public void afterStartingVertx(Vertx vertx) {
    configureContextualLogging(vertx);
    configureExceptionHandling(vertx);
  }

  /**
   * Configure the cluster manager for Vert.x
   *
   * @param options The {@link VertxOptions} instance that Vert.x will be instantiated with
   */
  private void configureClustering(VertxOptions options) {
    setProperty(HAZELCAST_LOG_TYPE, "slf4j");

    options.setClusterManager(new HazelcastClusterManager(ConfigUtil.loadConfig()));

    if (Objects.isNull(System.getenv(HAZELCAST_HEALTH_MONITORING_LEVEL))
        && Objects.isNull(System.getProperty(HAZELCAST_HEALTH_MONITORING_LEVEL))) {
      // If not overridden, disable Hazelcast HealthMonitor logging
      System.setProperty(HAZELCAST_HEALTH_MONITORING_LEVEL, DEFAULT_HAZELCAST_LEVEL);
    }
  }

  /**
   * Add metrics support to Vert.x
   *
   * @param options The {@link VertxOptions} instance that Vert.x will be instantiated with
   */
  private void configureMetricsSupport(VertxOptions options) {
    MetricsSupport.addMetricsConfiguration(options);
  }

  /**
   * Configure contextual logging. This will propagate correlation id over the event bus
   * communication via interceptors.
   *
   * @param vertx The Vert.x instance
   */
  private void configureContextualLogging(Vertx vertx) {
    vertx.eventBus().addInboundInterceptor(new InboundCorrelationId<>());
    vertx.eventBus().addOutboundInterceptor(new OutboundCorrelationId<>());
  }

  /**
   * Configures a global, fallback, exception handler that will print the stacktrace. Vert.x usually
   * swallows these exceptions and prints a reduced message.
   *
   * @param vertx The vert.x instance to configure
   */
  private void configureExceptionHandling(Vertx vertx) {
    vertx.exceptionHandler(new GenericExceptionHandler());
  }
}
