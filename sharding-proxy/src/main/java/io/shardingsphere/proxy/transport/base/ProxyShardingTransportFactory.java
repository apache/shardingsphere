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

package io.shardingsphere.proxy.transport.base;

import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.transaction.manager.base.servicecomb.ShardingTransportFactory;
import org.apache.servicecomb.saga.transports.SQLTransport;

import java.sql.Connection;

/**
 * Transport factory for sharding-sphere proxy.
 *
 * @author yangyi
 */
public final class ProxyShardingTransportFactory implements ShardingTransportFactory {
    
    private final ThreadLocal<SQLTransport> transports = new ThreadLocal<>();
    
    @Override
    public void cacheTransport(final Connection connection) {
        transports.set(new ProxySQLTransport(ProxyContext.getInstance().getRuleRegistry(ProxyContext.getInstance().getDefaultSchema()).getBackendDataSource()));
    }
    
    @Override
    public void remove() {
        transports.remove();
    }
    
    @Override
    public SQLTransport getTransport() {
        return transports.get();
    }
}
