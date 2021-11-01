package hiiretail.api.handler;

import hiiretail.api.WebTestBase;
import hiiretail.api.example.Cache;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

@DisplayName("ReturnEntitiesHandler")
class ReturnEntitiesByGetTest extends WebTestBase {

  @BeforeEach
  void setUp() {
    router.get().handler(new ReturnEntitiesByGet());
  }

  @Test
  @Timeout(5000)
  @DisplayName("Should return '200 OK' and a list of entities")
  void testReturnEntities(Vertx vertx, VertxTestContext testContext) {
    // Mock the eventbus that the handler relies on (Cache implementation).
    vertx
        .eventBus()
        .consumer(
            Cache.RETRIEVE_FROM_CACHE,
            message ->
                message.reply(
                    new JsonArray()
                        .add(new JsonObject().put("name", "Justin").put("age", 15))
                        .add(new JsonObject().put("name", "Alexa").put("age", 23))));

    webClient
        .get("/")
        .putHeaders(MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.ACCEPT, "application/json"))
        .expect(ResponsePredicate.JSON)
        .expect(ResponsePredicate.SC_OK)
        .send()
        .map(HttpResponse::bodyAsJsonArray)
        .onSuccess(
            jsonArray -> testContext.verify(() -> Assertions.assertThat(jsonArray).hasSize(2)))
        .onComplete(testContext.succeedingThenComplete());
  }
}
