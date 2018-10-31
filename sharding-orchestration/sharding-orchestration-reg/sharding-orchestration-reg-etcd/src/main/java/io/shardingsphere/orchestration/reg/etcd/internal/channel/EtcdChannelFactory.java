/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.reg.etcd.internal.channel;

import io.grpc.Channel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
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
        Channel channel = NettyChannelBuilder.forTarget(TARGET)
                .usePlaintext(true)
                .nameResolverFactory(new EtcdNameSolverFactory(TARGET, endpoints))
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .build();
        Channel result = etcdChannels.putIfAbsent(endpoints, channel);
        return null == result ? channel : result;
    }
}
