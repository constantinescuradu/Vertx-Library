package hiiretail.eventbus;

import static java.util.Objects.nonNull;

import hiiretail.api.handler.CorrelationIdDecorator;
import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryContext;

/**
 * Interceptor that adds correlation id to eventbus message header
 *
 * @param <T> The type of the message payload
 * @author thced
 */
public class OutboundCorrelationId<T> implements Handler<DeliveryContext<T>> {

  @Override
  public void handle(DeliveryContext<T> event) {
    String requestId = ContextualData.get(CorrelationIdDecorator.CORRELATION_ID.toString());
    if (nonNull(requestId)) {
      event.message().headers().add(CorrelationIdDecorator.CORRELATION_ID, requestId);
    }
    event.next();
  }
}
