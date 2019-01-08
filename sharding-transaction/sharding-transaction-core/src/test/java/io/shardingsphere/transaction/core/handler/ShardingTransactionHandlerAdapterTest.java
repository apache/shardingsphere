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

package io.shardingsphere.transaction.core.handler;

import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.spi.ShardingTransactionHandler;
import org.junit.Test;

import static org.mockito.Mockito.verify;

public class ShardingTransactionHandlerAdapterTest {
    
    private ShardingTransactionHandler fixedShardingTransactionHandler = new FixedShardingTransactionHandler();
    
    private ShardingTransactionManager shardingTransactionManager = fixedShardingTransactionHandler.getShardingTransactionManager();
    
    @Test
    public void assertDoXATransactionBegin() {
        fixedShardingTransactionHandler.doInTransaction(TransactionOperationType.BEGIN);
        verify(shardingTransactionManager).begin();
    }
    
    @Test
    public void assertDoXATransactionCommit() {
        fixedShardingTransactionHandler.doInTransaction(TransactionOperationType.COMMIT);
        verify(shardingTransactionManager).commit();
    }
    
    @Test
    public void assertDoXATransactionRollback() {
        fixedShardingTransactionHandler.doInTransaction(TransactionOperationType.ROLLBACK);
        verify(shardingTransactionManager).rollback();
    }
}
