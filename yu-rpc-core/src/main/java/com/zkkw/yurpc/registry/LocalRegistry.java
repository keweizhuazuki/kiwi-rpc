package com.zkkw.yurpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistry {

    // 根据服务名获取服务类然后使用反射调用对象
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    // 服务注册
    public static void register(String serviceName, Class<?> serviceClass) {
        map.put(serviceName, serviceClass);
    }

    // 服务获取
    public static Class<?> getService(String serviceName) {
        return map.get(serviceName);
    }

    // 删除服务
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
