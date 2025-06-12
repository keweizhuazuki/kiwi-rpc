package com.yupi.yurpc.model;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 378269526375857313L;

    private String serviceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] args;
}
