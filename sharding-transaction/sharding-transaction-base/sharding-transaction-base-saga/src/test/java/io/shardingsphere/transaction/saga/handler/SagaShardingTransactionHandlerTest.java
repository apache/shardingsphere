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
import io.shardingsphere.core.event.transaction.base.SagaSQLExecutionEvent;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SagaShardingTransactionHandlerTest {
    private final SagaConfiguration config = new SagaConfiguration();
    
    private final SagaShardingTransactionHandler handler = new SagaShardingTransactionHandler();
    
    private final List<List<Object>> params = new ArrayList<>();
    
    @Mock
    private BASETransactionManager<SagaTransactionEvent> sagaTransactionManager;
    
    @Mock
    private SagaSQLExecutionEventHandler sagaSQLExecutionEventHandler;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, SQLException {
        Field transactionManagerField = SagaShardingTransactionHandler.class.getDeclaredField("transactionManager");
        transactionManagerField.setAccessible(true);
        transactionManagerField.set(handler, sagaTransactionManager);
        Field sagaSQLExecutionEventHandlerField = SagaShardingTransactionHandler.class.getDeclaredField("sagaSQLExecutionEventHandler");
        sagaSQLExecutionEventHandlerField.setAccessible(true);
        sagaSQLExecutionEventHandlerField.set(handler, sagaSQLExecutionEventHandler);
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
        SagaTransactionEvent event = SagaTransactionEvent.createBeginSagaTransactionEvent(null, config);
        handler.doInTransaction(event);
        verify(sagaTransactionManager).begin(event);
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithoutBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        SagaTransactionEvent event = SagaTransactionEvent.createCommitSagaTransactionEvent(config);
        handler.doInTransaction(event);
    }
    
    @Test
    public void assertCommitWithBegin() throws SQLException {
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        handler.doInTransaction(SagaTransactionEvent.createBeginSagaTransactionEvent(null, config));
        when(sagaTransactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        SagaTransactionEvent event = SagaTransactionEvent.createCommitSagaTransactionEvent(config);
        handler.doInTransaction(event);
        verify(sagaTransactionManager).commit(event);
        verify(sagaSQLExecutionEventHandler).clean();
    }
    
    @Test
    public void assertRollback() throws SQLException {
        SagaTransactionEvent event = SagaTransactionEvent.createRollbackSagaTransactionEvent(config);
        handler.doInTransaction(event);
        verify(sagaTransactionManager).rollback(event);
        verify(sagaSQLExecutionEventHandler).clean();
    }
    
    @Test
    public void assertSagaSQLExecutionEvent() throws NoSuchFieldException, IllegalAccessException {
        SagaSQLExecutionEvent sqlExecutionEvent = new SagaSQLExecutionEvent(new RouteUnit("", new SQLUnit("", params)), "1", true);
        SagaTransactionEvent event = SagaTransactionEvent.createExecutionSagaTransactionEvent(sqlExecutionEvent);
        handler.doInTransaction(event);
        verify(sagaSQLExecutionEventHandler).handleSQLExecutionEvent(sqlExecutionEvent);
    }
}
