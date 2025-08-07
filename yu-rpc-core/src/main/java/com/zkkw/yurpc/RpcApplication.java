package com.zkkw.yurpc;

import com.zkkw.yurpc.config.RegistryConfig;
import com.zkkw.yurpc.config.RpcConfig;
import com.zkkw.yurpc.constant.RpcConstant;
import com.zkkw.yurpc.registry.Registry;
import com.zkkw.yurpc.registry.RegistryFactory;
import com.zkkw.yurpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {

    /**
     * RPC 配置
     */
    private static volatile RpcConfig rpcConfig;

    /**
     * 初始化 RPC 应用配置
     *
     * @param newRpcConfig 新的 RPC 配置
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("RPC application initialized with config: {}", rpcConfig);
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("Registry initialized with config: {}", registryConfig);
    }

    /**
     * 初始化 RPC 应用配置（加载默认配置）
     */
    public static void init(){
        RpcConfig newRpcConfig;
        try{
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        }catch (Exception e){
            // 配置加载失败，使用默认配置
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取 RPC 配置
     * <p>
     * 如果配置未初始化，则会先进行初始化（双重检查锁定）
     * </p>
     *
     * @return RPC 配置实例
     */
    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null){
            synchronized (RpcApplication.class){
                if(rpcConfig == null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
