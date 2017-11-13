package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.grpc.Channel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton builder
 *
 * @author junxiong
 */
public class EtcdClientBuilder {
    private static AtomicReference<EtcdClient> etcdClientRef = new AtomicReference<>(null);

    private List<String> endpoints;
    private long keepAliveTime = 200;
    private long keepAliveTimeout = 300;
    private boolean keepAlive = false;
    private boolean idle = false;
    private long idleTimeout = 1000 * 60L;
    private long timeout;
    private int maxRetry;

    public static EtcdClientBuilder newBuilder() {
        return new EtcdClientBuilder();
    }

    public EtcdClientBuilder endpoints(List<String> endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    public EtcdClientBuilder endpoints(String... endpoints) {
        this.endpoints = Arrays.asList(endpoints);
        return this;
    }

    public EtcdClientBuilder keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public EtcdClientBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public EtcdClientBuilder keepAliveTimeout(long keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
        return this;
    }

    public EtcdClientBuilder idle(boolean idle) {
        this.idle = idle;
        return this;
    }

    public EtcdClientBuilder idelTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public EtcdClient build() {
        final EtcdClient etcdClient = etcdClientRef.get();
        if (etcdClient == null) {
            final String target = "etcd";
            final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(target)
                    .usePlaintext(true)
                    .idleTimeout(idleTimeout, TimeUnit.MICROSECONDS)
                    .nameResolverFactory(DirectNameSolverFactory.newFactory(target, endpoints));
            if (keepAlive) {
                channelBuilder.keepAliveTimeout(keepAliveTimeout, TimeUnit.MILLISECONDS)
                        .keepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
            }
            if (idle) {
                channelBuilder.idleTimeout(idleTimeout, TimeUnit.MILLISECONDS);
            }
            final Channel channel = channelBuilder.build();
            final EtcdClient newEtcdClient = new EtcdClientImpl(channel);
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
