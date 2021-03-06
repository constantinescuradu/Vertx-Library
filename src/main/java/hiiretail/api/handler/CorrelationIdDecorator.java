package hiiretail.api.handler;

import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Middle-ware that sets up the correlation id when a request is received. Use correlation-id to
 * trace requests throughout the system.
 *
 * @author thced
 */
public class CorrelationIdDecorator implements Handler<RoutingContext> {

  public static final CharSequence CORRELATION_ID = HttpHeaders.createOptimized("Correlation-ID");

  private static final Logger log = LoggerFactory.getLogger(CorrelationIdDecorator.class);

  private CorrelationIdDecorator() {
    // Hidden
  }

  /**
   * Instantiate a new correlation id handler
   *
   * <p>Follows Vert.x conventional way of instantiating handlers
   */
  public static CorrelationIdDecorator create() {
    return new CorrelationIdDecorator();
  }

  @Override
  public void handle(RoutingContext ctx) {
    final MultiMap headers = ctx.request().headers();
    final String correlationId =
        headers.contains(CORRELATION_ID)
            ? headers.get(CORRELATION_ID)
            : CorrelationIdDecorator.correlationId();

    // Put the correlation id on the local map
    ContextualData.put(CORRELATION_ID.toString(), correlationId);

    // Send correlation id back to client again
    ctx.addHeadersEndHandler(
        v ->
            ctx.response()
                .putHeader(CORRELATION_ID, ContextualData.get(CORRELATION_ID.toString())));

    ctx.next();
  }

  private static String correlationId() {
    return UUID.randomUUID().toString();
  }
}
