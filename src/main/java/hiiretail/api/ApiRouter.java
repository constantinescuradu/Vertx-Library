package hiiretail.api;

import static java.util.Objects.nonNull;

import hiiretail.api.handler.*;
import hiiretail.api.handler.health.HealthChecksProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.impl.BlockingHandlerDecorator;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Router is the set of endpoints that makes up the HTTP API of the service.
 *
 * <p>Note! Once the Web API Service is moved out of Tech-preview status, this is a highly
 * recommended approach: <a href=https://vertx.io/docs/vertx-web-api-service/java/>WEB API
 * Service</a>
 *
 * @author thced
 */
public class ApiRouter extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiRouter.class);
  private static final String SCHEMA_OPENAPI_YAML = "schema/openapi.yaml";

  /**
   * If we have a lot of Routers then all of them will print all of their routes, we only need one
   * Router to print its routes..
   */
  public static final AtomicBoolean logSemaphore = new AtomicBoolean(true);

  public static final AtomicBoolean logStartSemaphore = new AtomicBoolean(true);

  /** Available in config.properties */
  private static final String PORT = "API_HTTP_PORT";

  @Override
  public void start(Promise<Void> startPromise) {
    router()
        .compose(this::startServer)
        .onSuccess(
            port -> {
              if (logStartSemaphore.compareAndSet(true, false)) {
                log.info("API served on port {}", port);
              }
            })
        .<Void>mapEmpty()
        .onComplete(startPromise);
  }

  /**
   * Start the HTTP API server
   *
   * @param router The router that handles all requests to the server
   * @return The future containing the port of the server, or a failure
   */
  private Future<Integer> startServer(Router router) {
    HttpServerOptions options = new HttpServerOptions().setPort(config().getInteger(PORT, 8080));
    return vertx
        .createHttpServer(options)
        .requestHandler(router)
        .listen()
        .map(HttpServer::actualPort);
  }

  /**
   * Setup the API router
   *
   * @return A future containing the final, complete, router, or a failure
   */
  private Future<Router> router() {
    return RouterBuilder.create(vertx, SCHEMA_OPENAPI_YAML)
        .map(builder -> builder.rootHandler(CorrelationIdDecorator.create()))
        .map(builder -> builder.rootHandler(LoggerHandler.create(LoggerFormat.TINY)))
        .map(builder -> builder.rootHandler(ResponseTimeHandler.create()))
        .map(this::addHandlers)
        .map(RouterBuilder::createRouter)
        .map(this::addNonApiHandlers)
        .map(this::printRoutes);
  }

  /**
   * Register all operations' handlers
   *
   * <p>Note! If you need to do blocking execution, use the handy {@link BlockingHandlerDecorator}
   *
   * @param builder The router builder
   * @return The router builder populated with all handlers
   */
  private RouterBuilder addHandlers(RouterBuilder builder) {

    var errorHandler = errorHandler();

    builder
        .operation("return.books")
        //.handler(new ReturnEntitiesByGet())
        .handler(new GetBook())
        .failureHandler(errorHandler);

    //builder.operation("add.entity").handler(new AddEntityByPost()).failureHandler(errorHandler);
    builder.operation("reader.borrow").handler(new PostBook()).failureHandler(errorHandler);

    return builder;
  }

  private ErrorHandler errorHandler() {
    // True when the sysProp/envVar 'vertxweb.environment'/'VERTXWEB_ENVIRONMENT' is set to 'dev'
    boolean development = WebEnvironment.development();
    // Display/return exceptions during development only
    return ErrorHandler.create(vertx, development);
  }

  /**
   * Register handlers (or other sub-routers) that are not part of the OpenAPI specification, and
   * need their own endpoint.
   *
   * <p>Note! If you need to do blocking execution, use the handy {@link BlockingHandlerDecorator}
   *
   * @param router The router that handles all requests
   * @return The router populated with non-API handlers
   */
  private Router addNonApiHandlers(Router router) {
    // Create the "alive" endpoint
    router.get("/alive").handler(HealthCheckHandler.create(vertx));
    // Create the "health" endpoint
    router
        .get("/health")
        .handler(HealthCheckHandler.createWithHealthChecks(HealthChecksProvider.create(vertx)));

    return router;
  }

  /**
   * Prints out the registered routes on the router
   *
   * @param router The router instance
   * @return The same router instance when done logging
   */
  private Router printRoutes(Router router) {
    if (logSemaphore.compareAndSet(true, false)) {
      router.getRoutes().stream()
          .filter(route -> nonNull(route.getName())) // base route does not have a name
          .forEach(this::logRoute);
    }
    return router;
  }

  private void logRoute(Route route) {
    log.debug("{} {}", route.methods(), route.getName());
  }
}
