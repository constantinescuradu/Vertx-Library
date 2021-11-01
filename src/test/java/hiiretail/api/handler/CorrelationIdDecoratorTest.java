package hiiretail.api.handler;

import hiiretail.api.WebTestBase;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

@DisplayName("CorrelationIdDecoratingHandler")
class CorrelationIdDecoratorTest extends WebTestBase {

  @BeforeEach
  void setUp() {
    router.get("/").handler(CorrelationIdDecorator.create()).handler(RoutingContext::end);
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  @DisplayName("Sending generated correlation id returns same id")
  void testCorrelationId(VertxTestContext testContext) {
    final String correlationId = UUID.randomUUID().toString();
    webClient
        .get("/")
        .putHeader(correlationIdHeader(), correlationId)
        .send()
        .onSuccess(
            response ->
                testContext.verify(
                    () -> {
                      Assertions.assertThat(response.statusCode()).isEqualTo(200);

                      MultiMap headers = response.headers();
                      Assertions.assertThat(headers.get(correlationIdHeader()))
                          .isEqualTo(correlationId);
                    }))
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  @DisplayName("Sending no correlation id should return generated id")
  void testGeneratedCorrelationId(VertxTestContext testContext) {
    webClient
        .get("/")
        .send()
        .onSuccess(
            response ->
                testContext.verify(
                    () -> {
                      Assertions.assertThat(response.statusCode()).isEqualTo(200);

                      MultiMap headers = response.headers();
                      Assertions.assertThat(headers.get(correlationIdHeader())).isNotBlank();
                    }))
        .onComplete(testContext.succeedingThenComplete());
  }

  private String correlationIdHeader() {
    return CorrelationIdDecorator.CORRELATION_ID.toString();
  }
}
