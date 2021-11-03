package hiiretail.api.handler;

import hiiretail.api.example.Cache;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public class GetBook implements Handler<RoutingContext> {

  private static final JsonObject EMPTY = new JsonObject();
  @Override
  public void handle(RoutingContext event) {
    Vertx vertx = event.vertx();

    requestEntitiesFromCache(vertx)
        .onSuccess(event::json)
        .onFailure(event::fail);
  }
  private Future<JsonArray> requestEntitiesFromCache(Vertx vertx) {

    DeliveryOptions options = new DeliveryOptions().setLocalOnly(true);

    return vertx
        .eventBus()
        .<JsonArray>request(Cache.RETRIEVE_FROM_CACHE, EMPTY, options)
        .map(Message::body);
  }
}
