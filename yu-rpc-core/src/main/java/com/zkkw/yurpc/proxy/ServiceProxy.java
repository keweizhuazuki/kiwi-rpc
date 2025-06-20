package com.zkkw.yurpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zkkw.yurpc.RpcApplication;
import com.zkkw.yurpc.model.RpcRequest;
import com.zkkw.yurpc.model.RpcResponse;
import com.zkkw.yurpc.serializer.JdkSerializer;
import com.zkkw.yurpc.serializer.Serializer;
import com.zkkw.yurpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取序列化器实例
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构建 RpcRequest 对象
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        // 序列化 RpcRequest 对象
        try {
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送 HTTP POST 请求到服务端
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                // TODO 硬编码修改
                byte[] result = httpResponse.bodyBytes();
                // 反序列化响应结果
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
