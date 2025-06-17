package com.zkkw.yurpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();

        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        server.requestHandler(new HttpServerHandler());
        server.listen(port, res -> {
            if (res.succeeded()) {
                System.out.println("Server is now listening on port" + port);
            } else {
                System.err.println("Failed to start HTTP server: " + res.cause());
            }
        });
    }
}
