package hiiretail.api;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.parallel.*;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(VertxExtension.class)
public class WebTestBase {

  protected WebClient webClient;
  protected Router router;

  @BeforeEach
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void setup(Vertx vertx, VertxTestContext testContext) throws IOException {
    this.router = router(vertx);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(getPort())
        .onSuccess(server -> webClient = webClient(vertx, server.actualPort()))
        .onComplete(testContext.succeedingThenComplete());
  }

  /**
   * Convenience method to create a Router with BodyHandler attached.
   *
   * @param vertx The vertx instance
   * @return The router
   */
  private Router router(Vertx vertx) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(ResponseTimeHandler.create());
    router.route().handler(ResponseContentTypeHandler.create());

    return router;
  }

  private WebClient webClient(Vertx vertx, int port) {
    WebClientOptions options = new WebClientOptions().setDefaultPort(port);
    return WebClient.create(vertx, options);
  }

  private int getPort() {
    int port = -1;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    } catch (IOException e) {
      // Silence
    }
    return port;
  }
}
