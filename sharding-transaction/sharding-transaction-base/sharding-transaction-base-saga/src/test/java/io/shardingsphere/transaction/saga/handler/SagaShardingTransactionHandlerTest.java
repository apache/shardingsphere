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

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.core.internal.context.SagaSQLExecutionContext;
import io.shardingsphere.transaction.core.internal.context.SagaTransactionContext;
import io.shardingsphere.transaction.core.internal.manager.ShardingTransactionManager;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SagaShardingTransactionHandlerTest {
    private final SagaConfiguration config = new SagaConfiguration();
    
    private final SagaShardingTransactionHandler handler = new SagaShardingTransactionHandler();
    
    private final List<List<Object>> params = new ArrayList<>();
    
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
        SagaTransactionContext event = SagaTransactionContext.createBeginSagaTransactionContext(null, config);
        handler.doInTransaction(event);
        verify(sagaTransactionManager).begin(event);
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithoutBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        SagaTransactionContext event = SagaTransactionContext.createCommitSagaTransactionContext(config);
        handler.doInTransaction(event);
    }
    
    @Test
    public void assertCommitWithBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        handler.doInTransaction(SagaTransactionContext.createBeginSagaTransactionContext(null, config));
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        SagaTransactionContext event = SagaTransactionContext.createCommitSagaTransactionContext(config);
        handler.doInTransaction(event);
        verify(sagaTransactionManager).commit(event);
    }
    
    @Test
    public void assertRollback() throws SQLException {
        SagaTransactionContext event = SagaTransactionContext.createRollbackSagaTransactionContext(config);
        handler.doInTransaction(event);
        verify(sagaTransactionManager).rollback(event);
    }
    
    @Test
    public void assertSagaSQLExecutionContext() throws NoSuchFieldException, IllegalAccessException {
        SagaSQLExecutionContext sqlExecutionContext = new SagaSQLExecutionContext(new RouteUnit("", new SQLUnit("", params)), "1", true);
        handler.doInTransaction(SagaTransactionContext.createExecutionSagaTransactionContext(sqlExecutionContext));
        verify(sagaTransactionManager).handleSQLExecutionEvent(sqlExecutionContext);
    }
    
    @Test
    public void assertDestroyComponent() {
        handler.doInTransaction(SagaTransactionContext.createDestroyComponentContext(config));
        verify(sagaTransactionManager).removeSagaExecutionComponent(config);
    }
}
