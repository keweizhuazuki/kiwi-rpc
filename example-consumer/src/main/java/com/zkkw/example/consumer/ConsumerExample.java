package com.zkkw.example.consumer;

import com.zkkw.yurpc.config.RpcConfig;
import com.zkkw.yurpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
