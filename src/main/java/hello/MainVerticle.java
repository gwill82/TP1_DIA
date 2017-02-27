package hello;

/**
 * Created by wgata on 27/02/17.
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws IOException {

        // create and load User.properties
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream("/home/wgata/Téléchargements/TP1/src/main/java/hello/User.properties");
        defaultProps.load(in);

        // Properties applicationProps = new Properties(defaultProps);
        //in = new FileInputStream("/home/wgata/Téléchargements/TP1/src/main/java/hello/User.properties");
        // applicationProps.load(in);

        //vertx.deployVerticle("hello.AnotherVerticle");

        vertx.eventBus().consumer("authentication.requests", message -> {
                final JsonObject response = new JsonObject();

                try {
                    JsonObject payload = (JsonObject) message.body();
                    String login = payload.getString("login");
                    String password = payload.getString("password");
                    final String validPassword = defaultProps.getProperty(login, null);
                    if (validPassword != null && validPassword.equals(password)) {
                        response.put("success", true);
                        response.remove("reason");
                    } else {
                        response.put("reason", "Bad login or password");
                    }
                } catch (Exception e) {
                    response.put("reason", "Bad request format");
                }

                message.reply(response);

            });

        vertx.createHttpServer().requestHandler(req -> {
                    if (req.path()=="/auth") {
                        if (req.method()== HttpMethod.POST) {
                            req.bodyHandler(handleAuthWebRequest(req));
                        }
                    }
                });

        private Handler<Buffer> handleAuthWebRequest(HttpServerRequest req) {
            return data -> {
                try {
                    final JsonObject message = new JsonObject(String.valueOf(data));
                    vertx.eventBus()
                            .send("authentication.requests",
                                    message,
                                    response -> {
                                        final JsonObject result = (JsonObject) response.result().body();
                                        req.response()
                                                .setStatusCode(200)
                                                .putHeader("Content-Type", "text/json")
                                                .end(String.valueOf(result));
                                    });
                } catch (DecodeException e) {
                    req.response()
                            .setStatusCode(406)
                            .putHeader("Content-Type", "text/json")
                            .end(new JsonObject().put("success", false).put("reason", "Can not parse request").toString());
                }
            };
        }


                .setStatusCode(200)
                .putHeader("Content-Type", "text/plain")
                .end("Yo!"))
                .listen(6958, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        System.out.println("HTTP Server running");
                        HttpServer server = asyncResult.result();
                        System.out.println(server);
                    } else {
                        System.err.println("Wooops");
                        Throwable err = asyncResult.cause();
                        System.err.println(err);
                    }
                });

    }
}