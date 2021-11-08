package hiiretail.api.handler;


import hiiretail.api.example.Cache;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public class PostBook implements Handler<RoutingContext> {

  Boolean isBooked = false;

  @Override
  public void handle(RoutingContext event) {
    Vertx vertx = event.vertx();
    JsonObject entity = event.getBodyAsJson();

    addEntitytoCache(vertx, entity)
        .onSuccess(event1 -> event.end("Book was borrowed by POST"))
        .onFailure(event::fail);
  }


  private Future<Void> addEntitytoCache(Vertx vertx, JsonObject entity){
    while(!isBooked){
      entity.put("isBooked", true);
      vertx.eventBus().request(Cache.ADD_TO_CACHE, entity).mapEmpty();
      isBooked = true;
    }
    return Future.succeededFuture();
  }
}
