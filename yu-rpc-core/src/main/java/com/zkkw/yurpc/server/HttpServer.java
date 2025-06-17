package com.zkkw.yurpc.server;

public interface HttpServer {
    /**
     * 启动 HTTP 服务器
     * @param port 监听端口
     */
    void doStart(int port);
}
