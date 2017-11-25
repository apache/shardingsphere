package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.grpc.Channel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton builder.
 *
 * @author junxiong
 */
public final class EtcdClientBuilder {
    
    private static AtomicReference<EtcdClient> etcdClientRef = new AtomicReference<>(null);
    
    private List<String> endpoints;
    
    private long timeout = 200L;
    
    private long span = 100L;
    
    private int maxRetry = 2;
    
    public static EtcdClientBuilder newBuilder() {
        return new EtcdClientBuilder();
    }
    
    public EtcdClientBuilder endpoints(final String... endpoints) {
        this.endpoints = Arrays.asList(endpoints);
        return this;
    }
    
    public EtcdClientBuilder span(final long span) {
        this.span = span;
        return this;
    }
    
    public EtcdClientBuilder maxRetry(final int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }
    
    public EtcdClientBuilder timeout(final long span) {
        this.timeout = span;
        return this;
    }
    
    public EtcdClient build() {
        EtcdClient etcdClient = etcdClientRef.get();
        if (null != etcdClient) {
            return etcdClient;
        }
        String target = "etcd";
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(target)
                .usePlaintext(true)
                .nameResolverFactory(DirectNameSolverFactory.newFactory(target, endpoints));
        Channel channel = channelBuilder.build();
        EtcdClient newEtcdClient = new EtcdClient(channel, timeout, span, maxRetry);
        if (etcdClientRef.compareAndSet(null, newEtcdClient)) {
            return newEtcdClient;
        }
        return etcdClientRef.get();
    }
}
