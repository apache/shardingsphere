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
public class EtcdClientBuilder {
    
    private static AtomicReference<EtcdClient> etcdClientRef = new AtomicReference<>(null);
    
    private List<String> endpoints;
    
    private long ttl = 200L;
    
    private long timeout = 200L;
    
    private long span = 100L;
    
    private int maxRetry = 2;

    public static EtcdClientBuilder newBuilder() {
        return new EtcdClientBuilder();
    }

    public EtcdClientBuilder endpoints(final List<String> endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    public EtcdClientBuilder endpoints(final String... endpoints) {
        this.endpoints = Arrays.asList(endpoints);
        return this;
    }

    public EtcdClientBuilder ttl(final long ttl) {
        this.ttl = ttl;
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
        final EtcdClient etcdClient = etcdClientRef.get();
        if (etcdClient == null) {
            final String target = "etcd";
            final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(target)
                    .usePlaintext(true)
                    .nameResolverFactory(DirectNameSolverFactory.newFactory(target, endpoints));
//            final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress("localhost", 2379)
//                    .usePlaintext(true);
            final Channel channel = channelBuilder.build();
            final EtcdClient newEtcdClient = new EtcdClientImpl(channel, timeout, span, maxRetry);
            if (etcdClientRef.compareAndSet(null, newEtcdClient)) {
                return newEtcdClient;
            } else {
                return etcdClientRef.get();
            }
        } else {
            return etcdClient;
        }
    }
}
