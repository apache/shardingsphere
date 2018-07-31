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

package io.shardingsphere.transaction.api.base;

import io.shardingsphere.transaction.api.TransactionManager;
import io.shardingsphere.transaction.common.event.TransactionEvent;

import java.sql.SQLException;

/**
 * Saga transaction manager for sharding-sphere.
 *
 * @author zhaojun
 */
public class SagaTransactionManager implements TransactionManager {
    
    @Override
    public void begin(final TransactionEvent transactionEvent) throws SQLException {
    
    }
    
    @Override
    public void commit(final TransactionEvent transactionEvent) throws SQLException {
    
    }
    
    @Override
    public void rollback(final TransactionEvent transactionEvent) throws SQLException {
    
    }
    
    @Override
    public int getStatus() throws SQLException {
        return 0;
    }
}
