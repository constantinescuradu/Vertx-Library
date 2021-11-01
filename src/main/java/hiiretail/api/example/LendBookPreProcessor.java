package hiiretail.api.example;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


public class LendBookPreProcessor implements PreProcessor{
  @Override
  public Future<JsonObject> apply(JsonObject entity) {
    if(entity.containsKey("isBooked")){
      Boolean isBooked = entity.getBoolean("isBooked");
      if(!isBooked){
        isBooked = true;
      }
      entity.put("isBooked", isBooked);
    }
    return Future.succeededFuture(entity);
  }
}
