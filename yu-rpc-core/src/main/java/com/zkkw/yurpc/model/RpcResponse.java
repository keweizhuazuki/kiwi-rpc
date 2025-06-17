package com.zkkw.yurpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 2724380621294536161L;


    /**
     * 要在 RPC 请求中传输的数据对象，可以是任意需要序列化发送的对象。
     */
    private Object data;

    /**
     * 被传输数据的类型，用于接收端正确反序列化数据。
     */
    private Class<?> dataType;

    /**
     * 远程服务需要调用的方法名。
     */
    private String message;

    /**
     * RPC 过程中的异常信息（如有）。
     */
    private Exception exception;
}
