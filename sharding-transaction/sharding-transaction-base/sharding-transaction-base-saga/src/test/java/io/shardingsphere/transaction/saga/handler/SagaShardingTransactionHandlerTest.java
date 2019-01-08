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

package io.shardingsphere.transaction.saga.handler;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.core.context.SagaTransactionContext;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.saga.config.SagaConfiguration;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.transaction.Status;
import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SagaShardingTransactionHandlerTest {
    
    private final SagaConfiguration config = new SagaConfiguration();
    
    private final SagaShardingTransactionHandler handler = new SagaShardingTransactionHandler();
    
    @Mock
    private SagaTransactionManager sagaTransactionManager;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, SQLException {
        Field transactionManagerField = SagaShardingTransactionHandler.class.getDeclaredField("transactionManager");
        transactionManagerField.setAccessible(true);
        transactionManagerField.set(handler, sagaTransactionManager);
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(handler.getTransactionType(), equalTo(TransactionType.BASE));
    }
    
    @Test
    public void assertGetTransactionManager() {
        assertThat(handler.getShardingTransactionManager(), CoreMatchers.<ShardingTransactionManager>equalTo(sagaTransactionManager));
    }
    
    @Test
    public void assertBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        SagaTransactionContext context = new SagaTransactionContext(TransactionOperationType.BEGIN, null);
        handler.doInTransaction(context);
        verify(sagaTransactionManager).begin(context);
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithoutBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        SagaTransactionContext context = new SagaTransactionContext(TransactionOperationType.COMMIT, null);
        handler.doInTransaction(context);
    }
    
    @Test
    public void assertCommitWithBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        handler.doInTransaction(new SagaTransactionContext(TransactionOperationType.BEGIN, null));
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        SagaTransactionContext context = new SagaTransactionContext(TransactionOperationType.COMMIT, null);
        handler.doInTransaction(context);
        verify(sagaTransactionManager).commit(context);
    }
    
    @Test
    public void assertRollback() throws SQLException {
        SagaTransactionContext context = new SagaTransactionContext(TransactionOperationType.ROLLBACK, null);
        handler.doInTransaction(context);
        verify(sagaTransactionManager).rollback(context);
    }
}
