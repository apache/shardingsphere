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

package io.shardingsphere.transaction.manager.base;

import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;

import javax.transaction.Status;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 */
public final class SagaTransactionManager implements ShardingTransactionManager {
    
    @Override
    public void begin(final ShardingTransactionEvent transactionEvent) {
    }
    
    @Override
    public void commit(final ShardingTransactionEvent transactionEvent) {
    }
    
    @Override
    public void rollback(final ShardingTransactionEvent transactionEvent) {
    }
    
    @Override
    public int getStatus() {
        // TODO :zhaojun need confirm, return Status.STATUS_NO_TRANSACTION or zero? 
        return Status.STATUS_NO_TRANSACTION;
    }
}
