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

package io.shardingsphere.transaction.manager.base.fixture;

import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.transaction.manager.base.servicecomb.EmptySQLTransport;
import io.shardingsphere.transaction.manager.base.servicecomb.ShardingTransportFactory;
import org.apache.servicecomb.saga.transports.SQLTransport;

public final class FixtureShardingTransportFactory implements ShardingTransportFactory {
    
    @Override
    public void cacheTransport(final SagaTransactionEvent event) {
    }
    
    @Override
    public void remove() {
    }
    
    @Override
    public SQLTransport getTransport() {
        return new EmptySQLTransport();
    }
}
