package hiiretail.eventbus;

import static java.util.Objects.nonNull;

import hiiretail.api.handler.CorrelationIdDecorator;
import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryContext;

/**
 * Interceptor that reads correlation id message header and sets it on Vert.x thread context
 *
 * @param <T> The type of the message payload
 * @author thced
 */
public class InboundCorrelationId<T> implements Handler<DeliveryContext<T>> {

  @Override
  public void handle(DeliveryContext<T> event) {
    String requestId = event.message().headers().get(CorrelationIdDecorator.CORRELATION_ID);
    if (nonNull(requestId)) {
      ContextualData.put(CorrelationIdDecorator.CORRELATION_ID.toString(), requestId);
    }
    event.next();
  }
}
