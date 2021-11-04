package hiiretail.api.handler;

import hiiretail.api.example.Cache;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class PostBook implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext event) {
    Vertx vertx = event.vertx();
    JsonObject entity = event.getBodyAsJson();

    processEntityFromCache(vertx, entity)
        .onSuccess(event1 -> event.end("Book was borrowed by POST"))
        .onFailure(event::fail);
  }

  private Future<Void> processEntityFromCache(Vertx vertx, JsonObject entity){
    return vertx.eventBus().request(Cache.LEND_FROM_CACHE, entity).mapEmpty();
  }
}
