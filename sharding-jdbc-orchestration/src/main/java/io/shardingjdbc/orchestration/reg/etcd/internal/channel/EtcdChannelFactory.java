package io.shardingjdbc.orchestration.reg.etcd.internal.channel;

import io.grpc.Channel;
import io.grpc.netty.NettyChannelBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Etcd channel factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EtcdChannelFactory {
    
    private static final String TARGET = "etcd";
    
    private static ConcurrentHashMap<List<String>, Channel> etcdChannels = new ConcurrentHashMap<>();
    
    /**
     * Get etcd channel instance.
     * 
     * @param endpoints etcd endpoints
     * @return etcd channel
     */
    public static Channel getInstance(final List<String> endpoints) {
        if (etcdChannels.containsKey(endpoints)) {
            return etcdChannels.get(endpoints);
        }
        Channel channel = NettyChannelBuilder.forTarget(TARGET).usePlaintext(true).nameResolverFactory(new EtcdNameSolverFactory(TARGET, endpoints)).build();
        Channel result =  etcdChannels.putIfAbsent(endpoints, channel);
        return null == result ? channel : result;
    }
}
