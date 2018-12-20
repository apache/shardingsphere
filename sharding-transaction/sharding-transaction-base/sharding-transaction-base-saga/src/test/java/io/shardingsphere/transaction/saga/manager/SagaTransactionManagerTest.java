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

package io.shardingsphere.transaction.saga.manager;

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.transaction.core.internal.context.SagaSQLExecutionContext;
import io.shardingsphere.transaction.core.internal.context.SagaTransactionContext;
import io.shardingsphere.transaction.saga.handler.SagaSQLExecutionContextHandler;
import io.shardingsphere.transaction.saga.revert.RevertEngine;
import io.shardingsphere.transaction.saga.servicecomb.SagaExecutionComponentHolder;
import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingTransportFactory;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.transaction.Status;
import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SagaTransactionManagerTest {
    
    @Mock
    private SagaExecutionComponentHolder sagaExecutionComponentHolder;
    
    @Mock
    private SagaExecutionComponent sagaExecutionComponent;
    
    @Mock
    private SagaSQLExecutionContextHandler sagaSQLExecutionContextHandler;
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    private static SagaConfiguration config = new SagaConfiguration();
    
    private Map<String, SagaDefinitionBuilder> sagaDefinitionBuilderMap;
    
    private Map<String, RevertEngine> revertEngineMap;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Field sagaExecutionComponentHolderField = SagaTransactionManager.class.getDeclaredField("sagaExecutionComponentHolder");
        sagaExecutionComponentHolderField.setAccessible(true);
        sagaExecutionComponentHolderField.set(transactionManager, sagaExecutionComponentHolder);
        Field sagaSQLExecutionContextHandlerField = SagaTransactionManager.class.getDeclaredField("sagaSQLExecutionContextHandler");
        sagaSQLExecutionContextHandlerField.setAccessible(true);
        sagaSQLExecutionContextHandlerField.set(transactionManager, sagaSQLExecutionContextHandler);
        when(sagaExecutionComponentHolder.getSagaExecutionComponent(config)).thenReturn(sagaExecutionComponent);
        Field sagaDefinitionBuilderMapField = SagaTransactionManager.class.getDeclaredField("sagaDefinitionBuilderMap");
        sagaDefinitionBuilderMapField.setAccessible(true);
        sagaDefinitionBuilderMap = (Map<String, SagaDefinitionBuilder>) sagaDefinitionBuilderMapField.get(transactionManager);
        Field revertEngineMapField = SagaTransactionManager.class.getDeclaredField("revertEngineMap");
        revertEngineMapField.setAccessible(true);
        revertEngineMap = (Map<String, RevertEngine>) revertEngineMapField.get(transactionManager);
    }
    
    @Test
    public void assertBegin() {
        transactionManager.begin(SagaTransactionContext.createBeginSagaTransactionContext(null, config));
        assertThat(36 == transactionManager.getTransactionId().length(), is(true));
        assertThat(sagaDefinitionBuilderMap.size(), is(1));
        assertNotNull(transactionManager.getReverEngine(transactionManager.getTransactionId()));
        assertThat(revertEngineMap.size(), is(1));
        assertNotNull(transactionManager.getSagaDefinitionBuilder(transactionManager.getTransactionId()));
        assertNotNull(ShardingTransportFactory.getInstance().getTransport());
    }
    
    @Test
    public void assertCommit() {
        transactionManager.begin(SagaTransactionContext.createBeginSagaTransactionContext(null, config));
        transactionManager.commit(SagaTransactionContext.createCommitSagaTransactionContext(config));
        verify(sagaExecutionComponent).run(anyString());
        assertNull(transactionManager.getTransactionId());
        assertNull(ShardingTransportFactory.getInstance().getTransport());
        assertThat(sagaDefinitionBuilderMap.size(), is(0));
        assertThat(revertEngineMap.size(), is(0));
        verify(sagaSQLExecutionContextHandler).clean();
    }
    
    @Test
    public void assertRollback() {
        transactionManager.rollback(SagaTransactionContext.createRollbackSagaTransactionContext(config));
        assertNull(transactionManager.getTransactionId());
        assertNull(ShardingTransportFactory.getInstance().getTransport());
        assertThat(sagaDefinitionBuilderMap.size(), is(0));
        assertThat(revertEngineMap.size(), is(0));
        verify(sagaSQLExecutionContextHandler, never()).clean();
        transactionManager.begin(SagaTransactionContext.createBeginSagaTransactionContext(null, config));
        transactionManager.rollback(SagaTransactionContext.createRollbackSagaTransactionContext(config));
        assertNull(transactionManager.getTransactionId());
        assertNull(ShardingTransportFactory.getInstance().getTransport());
        assertThat(sagaDefinitionBuilderMap.size(), is(0));
        assertThat(revertEngineMap.size(), is(0));
        verify(sagaSQLExecutionContextHandler).clean();
    }
    
    @Test
    public void assertGetStatus() {
        transactionManager.begin(SagaTransactionContext.createBeginSagaTransactionContext(null, config));
        assertThat(transactionManager.getStatus(), is(Status.STATUS_ACTIVE));
        transactionManager.rollback(SagaTransactionContext.createRollbackSagaTransactionContext(config));
        assertThat(transactionManager.getStatus(), is(Status.STATUS_NO_TRANSACTION));
    }
    
    @Test
    public void assertRemoveSagaExecutionComponent() {
        transactionManager.removeSagaExecutionComponent(config);
        verify(sagaExecutionComponentHolder).removeSagaExecutionComponent(config);
    }
    
    @Test
    public void assertHandleSQLExecutionEvent() {
        SagaSQLExecutionContext context = mock(SagaSQLExecutionContext.class);
        transactionManager.handleSQLExecutionEvent(context);
        verify(sagaSQLExecutionContextHandler).handle(context);
    }
}
