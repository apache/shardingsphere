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

package io.shardingsphere.transaction.xa.handler;

import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.core.context.XATransactionContext;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.xa.manager.AtomikosTransactionManager;
import org.junit.Test;

import javax.transaction.Status;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XAShardingTransactionHandlerTest {
    
    private XAShardingTransactionHandler xaShardingTransactionHandler = new XAShardingTransactionHandler();
    
    private XATransactionContext beginContext = new XATransactionContext(TransactionOperationType.BEGIN);
    
    private XATransactionContext commitContext = new XATransactionContext(TransactionOperationType.COMMIT);
    
    private XATransactionContext rollbackContext = new XATransactionContext(TransactionOperationType.ROLLBACK);
    
    @Test
    public void assertGetTransactionManager() {
        ShardingTransactionManager shardingTransactionManager = xaShardingTransactionHandler.getShardingTransactionManager();
        assertThat(shardingTransactionManager, instanceOf(AtomikosTransactionManager.class));
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(xaShardingTransactionHandler.getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertDoXATransactionBegin() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                xaShardingTransactionHandler.doInTransaction(beginContext);
                int actualStatus = xaShardingTransactionHandler.getShardingTransactionManager().getStatus();
                assertThat(actualStatus, is(Status.STATUS_ACTIVE));
            }
        });
        thread.start();
        thread.join();
    }
    
    @Test
    public void assertDoXATransactionCommit() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                xaShardingTransactionHandler.doInTransaction(beginContext);
                xaShardingTransactionHandler.doInTransaction(commitContext);
                int actualStatus = xaShardingTransactionHandler.getShardingTransactionManager().getStatus();
                assertThat(actualStatus, is(Status.STATUS_NO_TRANSACTION));
            }
        });
        thread.start();
        thread.join();
    }
    
    @Test
    public void assertDoXATransactionRollback() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                xaShardingTransactionHandler.doInTransaction(beginContext);
                xaShardingTransactionHandler.doInTransaction(rollbackContext);
                int actualStatus = xaShardingTransactionHandler.getShardingTransactionManager().getStatus();
                assertThat(actualStatus, is(Status.STATUS_NO_TRANSACTION));
            }
        });
        thread.start();
        thread.join();
    }
    
    @Test
    public void assertDoXATransactionCommitRollback() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                xaShardingTransactionHandler.doInTransaction(beginContext);
                xaShardingTransactionHandler.doInTransaction(commitContext);
                xaShardingTransactionHandler.doInTransaction(rollbackContext);
                int actualStatus = xaShardingTransactionHandler.getShardingTransactionManager().getStatus();
                assertThat(actualStatus, is(Status.STATUS_NO_TRANSACTION));
            }
        });
        thread.start();
        thread.join();
    }
}
