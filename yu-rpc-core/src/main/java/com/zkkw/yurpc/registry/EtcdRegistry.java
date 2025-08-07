package com.zkkw.yurpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.zkkw.yurpc.config.RegistryConfig;
import com.zkkw.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * 注册服务缓存
     */
    private final RegistryServiceCache registryServiceCache= new RegistryServiceCache();

    /**
     * 本地注册的节点键集合
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        // 初始化心跳
        heartBeat();
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

        localRegisterNodeKeySet.add(registryKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(registerKey);
    }


    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存中读取
        List<ServiceMetaInfo> cachedServiceMetaInfoList  = registryServiceCache.readCache();
        if(cachedServiceMetaInfoList !=null){
            return cachedServiceMetaInfoList;
        }
        // 构建搜索前缀
        String searchPrefix = ETCD_ROOT_PATH + serviceKey;

        try{
            // 获取所有匹配的键值对
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get().getKvs();
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream().map(
                    keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }
            ).collect(Collectors.toList());
            // 写入缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        }catch (Exception e) {
            throw new RuntimeException("Failed to discover service: " + serviceKey, e);
        }

    }

    @Override
    public void destroy() {
        System.out.println("Destroying ETCD registry client...");
        // 下线节点
        for(String key:localRegisterNodeKeySet){
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e){
                throw new RuntimeException("Failed to unregister service node: " + key, e);
            }
        }
        // 取消所有本地注册的节点
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                for(String key:localRegisterNodeKeySet){
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get().getKvs();
                        if(CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    }catch (Exception e) {
                        System.err.println("Failed to send heartbeat for key: " + key + ", error: " + e.getMessage());
                    }
                }
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 监听指定的服务节点键
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response ->{
                for(WatchEvent event: response.getEvents()){
                    switch(event.getEventType()){
                        case DELETE:
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

}
