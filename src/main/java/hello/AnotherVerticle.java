package hello;

/**
 * Created by wgata on 27/02/17.
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class AnotherVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().consumer("authentication.requests", message -> {
            JsonObject payload = (JsonObject) message.body();
            System.out.println(payload.encodePrettily());
        });
    }
}
