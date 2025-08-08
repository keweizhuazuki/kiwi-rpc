package com.zkkw.yurpc.server.tcp;

import com.zkkw.yurpc.model.RpcRequest;
import com.zkkw.yurpc.model.RpcResponse;
import com.zkkw.yurpc.protocol.ProtocolMessage;
import com.zkkw.yurpc.protocol.ProtocolMessageDecoder;
import com.zkkw.yurpc.protocol.ProtocolMessageEncoder;
import com.zkkw.yurpc.protocol.ProtocolMessageTypeEnum;
import com.zkkw.yurpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {

        // 1. 定义真正处理业务的 Handler，这是“被装饰者”
        // 这段代码的核心任务是处理一个“完整”的 Buffer
        Handler<Buffer> requestHandler = buffer -> {
            // 接受请求，解码
            // 由于 Wrapper 的存在，这里的 buffer 100% 是一个完整的消息包
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                // 推荐使用日志框架记录错误，而不是直接抛出运行时异常
                System.err.println("协议消息解码错误: " + e.getMessage());
                return; // 解码失败，直接返回
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.getService(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                System.err.println("协议消息编码错误: " + e.getMessage());
            }
        };

        // 2. 创建“装饰者”，并用它包装“被装饰者”
        Handler<Buffer> handlerWithWrapper = new TcpBufferHandlerWrapper(requestHandler);

        // 3. 将“装饰者”注册为 NetSocket 的处理器
        // 这样，所有来自网络的数据都会先经过 Wrapper 的处理
        netSocket.handler(handlerWithWrapper);
    }
}