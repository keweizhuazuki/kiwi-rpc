package com.zkkw.yurpc.registry;

import cn.hutool.json.JSONUtil;
import com.zkkw.yurpc.config.RegistryConfig;
import com.zkkw.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry{
    /**
     * ETCD 客户端实例
     */
    private Client client;
    /**
     * ETCD KV 客户端实例
     */
    private KV kvClient;

    /**
     * ETCD 根路径
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建lease client（带有租约的客户端）
        Lease leaseClient = client.getLeaseClient();
        // 创建租约，设置30秒的租约时间
        long leaseId = leaseClient.grant(30).get().getID();
        // 设置要储存的键值对
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }


    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 构建搜索前缀
        String searchPrefix = ETCD_ROOT_PATH + serviceKey;

        try{
            // 获取所有匹配的键值对
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get().getKvs();
            return keyValues.stream().map(
                    keyValue -> {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }
            ).collect(Collectors.toList());
        }catch (Exception e) {
            throw new RuntimeException("Failed to discover service: " + serviceKey, e);
        }

    }

    @Override
    public void destroy() {
        System.out.println("Destroying ETCD registry client...");
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }
    }
}
