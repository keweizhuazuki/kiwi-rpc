package com.zkkw.example.provider;

import com.zkkw.yurpc.registry.LocalRegistry;
import com.zkkw.yurpc.server.HttpServer;
import com.zkkw.yurpc.server.VertxHttpServer;
import com.zkkw.example.common.service.UserService;

public class EasyProviderExample {
    public static void main(String[] args) {
        // 启动 web 服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
