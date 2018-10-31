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

package io.shardingsphere.shardingjdbc.transport;

import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.shardingjdbc.jdbc.adapter.AbstractConnectionAdapter;
import io.shardingsphere.transaction.manager.base.servicecomb.ShardingTransportFactory;
import org.apache.servicecomb.saga.transports.SQLTransport;

/**
 * Transport factory for sharding-sphere jdbc.
 *
 * @author yangyi
 */
public final class JDBCTransportFactory implements ShardingTransportFactory {
    
    private final ThreadLocal<SQLTransport> transports = new ThreadLocal<>();
    
    @Override
    public SQLTransport getTransport() {
        return transports.get();
    }
    
    @Override
    public void cacheTransport(final SagaTransactionEvent event) {
        transports.set(new JDBCSqlTransport((AbstractConnectionAdapter) event.getConnection()));
    }
    
    @Override
    public void remove() {
        transports.remove();
    }
}
