package com.zkkw.yurpc.config;

import com.zkkw.yurpc.fault.retry.RetryStrategyKeys;
import com.zkkw.yurpc.loadbalancer.ConsistentHashLoadBalancer;
import com.zkkw.yurpc.loadbalancer.LoadBalancer;
import com.zkkw.yurpc.loadbalancer.LoadBalancerKeys;
import com.zkkw.yurpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {
    /**
     * 名称
     */
    private String name = "yu-rpc";
    /**
     * 版本号
     */
    private String version = "1.0";
    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";
    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    private boolean mock = false;

    private String serializer = SerializerKeys.JDK;

    private RegistryConfig registryConfig = new RegistryConfig();

    public String loadBalancer = LoadBalancerKeys.CONSISTENT_HASH;

    private String retryStrategy = RetryStrategyKeys.NO;
}
