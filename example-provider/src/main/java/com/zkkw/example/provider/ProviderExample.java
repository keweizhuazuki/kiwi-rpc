package com.zkkw.example.provider;

import com.zkkw.yurpc.registry.LocalRegistry;
import com.zkkw.yurpc.server.VertxHttpServer;
import com.zkkw.example.common.service.UserService;
import com.zkkw.yurpc.RpcApplication;

public class ProviderExample {
    public static void main(String[] args) {
        RpcApplication.init();
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
