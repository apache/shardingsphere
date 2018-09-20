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

import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import org.junit.Test;

import javax.transaction.Status;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SagaTransactionManagerTest {
    
    @Test
    public void assertBegin() {
        new SagaTransactionManager().begin(new ShardingTransactionEvent() {
    
            @Override
            public TransactionOperationType getOperationType() {
                return TransactionOperationType.BEGIN;
            }
        });
    }
    
    @Test
    public void assertCommit() {
        new SagaTransactionManager().commit(new ShardingTransactionEvent() {
            
            @Override
            public TransactionOperationType getOperationType() {
                return TransactionOperationType.COMMIT;
            }
        });
    }
    
    @Test
    public void assertRollback() {
        new SagaTransactionManager().rollback(new ShardingTransactionEvent() {
            
            @Override
            public TransactionOperationType getOperationType() {
                return TransactionOperationType.ROLLBACK;
            }
        });
    }
    
    @Test
    public void assertGetStatus() {
        assertThat(new SagaTransactionManager().getStatus(), is(Status.STATUS_NO_TRANSACTION));
    }
}
