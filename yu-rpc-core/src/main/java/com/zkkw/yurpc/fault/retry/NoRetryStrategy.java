package com.zkkw.yurpc.fault.retry;

import com.zkkw.yurpc.model.RpcResponse;

import java.util.concurrent.Callable;

@Slf4j
public class NoRetryStrategy implements RetryStrategy {
    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
