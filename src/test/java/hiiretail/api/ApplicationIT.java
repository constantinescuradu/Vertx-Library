package hiiretail.api;

import hiiretail.Application;
import hiiretail.api.handler.CorrelationIdDecorator;
import hiiretail.eventbus.InboundCorrelationId;
import hiiretail.eventbus.OutboundCorrelationId;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.junit5.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.*;

@ExtendWith(VertxExtension.class)
@DisplayName("Application test(s)")
class ApplicationIT {

  private WebClient webClient;
  private String applicationDeployId;

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext testContext) {
    webClient = WebClient.create(vertx, new WebClientOptions().setDefaultPort(8080));

    vertx.eventBus().addInboundInterceptor(new InboundCorrelationId<>());
    vertx.eventBus().addOutboundInterceptor(new OutboundCorrelationId<>());

    vertx
        .deployVerticle(new Application(), new DeploymentOptions())
        .onSuccess(deploymentId -> this.applicationDeployId = deploymentId)
        .compose(this::insertInitialData)
        .onComplete(testContext.succeedingThenComplete());
  }

  private Future<Void> insertInitialData(String ignore) {
    return webClient
        .post("/api")
        .sendJson(new JsonObject().put("name", "Simone").put("age", 25))
        .mapEmpty();
  }

  @AfterEach
  void reset(Vertx vertx, VertxTestContext testContext) {
    vertx.undeploy(applicationDeployId).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
  @DisplayName("GET against API should succeed and return entities")
  void testGetEntities(VertxTestContext testContext) {
    webClient
        .get("/api")
        .expect(ResponsePredicate.SC_OK)
        .expect(ResponsePredicate.JSON)
        .send()
        .onSuccess(
            response ->
                testContext.verify(
                    () -> {
                      MultiMap headers = response.headers();
                      Assertions.assertThat(headers.get("correlation-id")).isNotBlank();
                      Assertions.assertThat(headers.get("x-response-time")).endsWith("ms");

                      JsonArray body = response.bodyAsJsonArray();
                      Assertions.assertThat(body).isNotNull();
                      Assertions.assertThat(body).hasSizeGreaterThanOrEqualTo(1);

                      JsonObject json = body.getJsonObject(0);
                      Assertions.assertThat(json.getInteger("age")).isBetween(50, 60);
                      Assertions.assertThat(json.getString("name")).isEqualTo("SIMONE");
                    }))
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
  @DisplayName("POST entity against API should succeed")
  void testPostEntity(VertxTestContext testContext) {
    String correlationId = UUID.randomUUID().toString();
    MultiMap requestHeaders =
        MultiMap.caseInsensitiveMultiMap()
            .add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
            .add(CorrelationIdDecorator.CORRELATION_ID, correlationId);

    webClient
        .post("/api")
        .putHeaders(requestHeaders)
        .expect(ResponsePredicate.SC_OK)
        .sendJson(new JsonObject().put("name", "Simone").put("age", 26))
        .map(HttpResponse::headers)
        .onSuccess(
            headers ->
                testContext.verify(
                    () -> {
                      String correlationIdHeader =
                          headers.get(CorrelationIdDecorator.CORRELATION_ID);
                      Assertions.assertThat(correlationIdHeader).isEqualTo(correlationId);
                    }))
        .onComplete(testContext.succeedingThenComplete());
  }
}
