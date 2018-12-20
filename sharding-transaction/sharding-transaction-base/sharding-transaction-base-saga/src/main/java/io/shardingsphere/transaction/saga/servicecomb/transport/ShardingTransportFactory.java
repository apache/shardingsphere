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

package io.shardingsphere.transaction.saga.servicecomb.transport;

import io.shardingsphere.transaction.core.internal.context.SagaTransactionContext;
import org.apache.servicecomb.saga.transports.SQLTransport;
import org.apache.servicecomb.saga.transports.TransportFactory;

/**
 * Extend interface for service comb saga TransportFactory.
 *
 * @author yangyi
 */
public final class ShardingTransportFactory implements TransportFactory<SQLTransport> {
    
    private static final ShardingTransportFactory INSTANCE = new ShardingTransportFactory();
    
    private final ThreadLocal<SQLTransport> transports = new ThreadLocal<>();
    
    /**
     * Get sharding transport factory instance.
     *
     * @return sharding transport factory
     */
    public static ShardingTransportFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public SQLTransport getTransport() {
        return transports.get();
    }
    
    /**
     * cache Transport.
     * @param context saga context
     */
    public void cacheTransport(final SagaTransactionContext context) {
        transports.set(new DataSourceMapSQLTransport(context.getDataSourceMap()));
    }
    
    /**
     * remove cached SQLTransport.
     */
    public void remove() {
        transports.remove();
    }
}
