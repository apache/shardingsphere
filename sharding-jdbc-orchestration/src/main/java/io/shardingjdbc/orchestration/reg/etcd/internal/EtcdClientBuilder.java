package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.grpc.Channel;
import io.grpc.netty.NettyChannelBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton builder.
 *
 * @author junxiong
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EtcdClientBuilder {
    
    private static final String TARGET = "etcd";
    
    private static AtomicReference<EtcdClient> etcdClientRef = new AtomicReference<>(null);
    
    private List<String> endpoints;
    
    private int timeoutMilliseconds = 500;
    
    private int maxRetries = 3;
    
    private int retryIntervalMilliseconds = 200;
    
    public static EtcdClientBuilder newBuilder() {
        return new EtcdClientBuilder();
    }
    
    public EtcdClientBuilder endpoints(final String... endpoints) {
        this.endpoints = Arrays.asList(endpoints);
        return this;
    }
    
    public EtcdClientBuilder timeoutMilliseconds(final int timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
        return this;
    }
    
    public EtcdClientBuilder maxRetryTimes(final int maxRetryTimes) {
        this.maxRetries = maxRetryTimes;
        return this;
    }
    
    public EtcdClientBuilder retryIntervalMilliseconds(final int retryIntervalMilliseconds) {
        this.retryIntervalMilliseconds = retryIntervalMilliseconds;
        return this;
    }
    
    public EtcdClient build() {
        EtcdClient etcdClient = etcdClientRef.get();
        if (null != etcdClient) {
            return etcdClient;
        }
        Channel channel = NettyChannelBuilder.forTarget(TARGET).usePlaintext(true).nameResolverFactory(new EtcdNameSolverFactory(TARGET, endpoints)).build();
        EtcdClient result = new EtcdClient(channel, timeoutMilliseconds, maxRetries, retryIntervalMilliseconds);
        return etcdClientRef.compareAndSet(null, result) ? result : etcdClientRef.get();
    }
}
