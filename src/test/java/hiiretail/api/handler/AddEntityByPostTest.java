package hiiretail.api.handler;

import hiiretail.api.WebTestBase;
import hiiretail.api.example.Cache;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.junit5.*;
import org.junit.jupiter.api.*;

/**
 * Note that this test only verify basic behavior. For a full test, consider the API test which
 * brings up the OpenAPI Router and validates input.
 *
 * @author thced
 */
@DisplayName("AddEntityHandler")
class AddEntityByPostTest extends WebTestBase {

  @BeforeEach
  void setUp() {
    router.post().handler(new AddEntityByPost());
  }

  @Test
  @DisplayName("Adding entity should return 200 OK")
  void testResponse(Vertx vertx, VertxTestContext testContext) {
    // Mock the eventbus that the handler relies on (Cache implementation).
    vertx.eventBus().consumer(Cache.ADD_TO_CACHE, message -> message.reply("OK"));

    webClient
        .post("/")
        .expect(ResponsePredicate.SC_OK)
        .sendJsonObject(new JsonObject().put("name", "MyName").put("age", 38))
        .onComplete(testContext.succeedingThenComplete());
  }
}
