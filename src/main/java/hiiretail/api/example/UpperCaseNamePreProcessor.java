package hiiretail.api.example;

import static io.vertx.core.Future.succeededFuture;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * A processor that capitalizes the name, if there exists such a key within the input
 *
 * @author thced
 */
public class UpperCaseNamePreProcessor implements PreProcessor {

  public Integer count = 0;
  public static ArrayList <Integer> totalBooks = new ArrayList<>();

  @Override
  public Future<JsonObject> apply(JsonObject entity) {
    if (entity.containsKey("name")) {
      var name = entity.getString("name");
      entity.put("name", name.toUpperCase());
      count++;
      totalBooks.add(count);
      System.out.println(totalBooks);
    }
    return succeededFuture(entity);
  }
}
