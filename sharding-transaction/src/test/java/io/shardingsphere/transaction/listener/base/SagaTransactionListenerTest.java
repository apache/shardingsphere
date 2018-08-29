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

package io.shardingsphere.transaction.listener.base;

import com.google.common.eventbus.EventBus;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.event.base.SagaSQLExecutionEvent;
import io.shardingsphere.transaction.event.base.SagaTransactionEvent;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;
import io.shardingsphere.transaction.manager.base.servicecomb.SagaDefinitionBuilder;
import org.apache.servicecomb.saga.core.SagaRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SagaTransactionListenerTest {
    
    private final EventBus eventBus = ShardingEventBusInstance.getInstance();
    
    private final SagaTransactionListener listener = new SagaTransactionListener();
    
    @Mock
    private BASETransactionManager<SagaTransactionEvent> sagaTransactionManager;
    
    private static String id = "1";
    
    private Map<String, SagaDefinitionBuilder> builderMap;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        when(sagaTransactionManager.getTransactionId()).thenReturn(id);
        Field transactionManager = SagaTransactionListener.class.getDeclaredField("transactionManager");
        Field sagaDefinitionBuilderMap = SagaTransactionListener.class.getDeclaredField("sagaDefinitionBuilderMap");
        transactionManager.setAccessible(true);
        sagaDefinitionBuilderMap.setAccessible(true);
        transactionManager.set(listener, sagaTransactionManager);
        builderMap = (Map<String, SagaDefinitionBuilder>) sagaDefinitionBuilderMap.get(listener);
        listener.register();
    }
    
    @After
    public void tearDown() {
        eventBus.unregister(listener);
    }
    
    @Test
    public void assertListenBegin() throws SQLException {
        SagaTransactionEvent event = new SagaTransactionEvent(TransactionOperationType.BEGIN);
        eventBus.post(event);
        verify(sagaTransactionManager).begin(event);
        assertThat(builderMap.size(), is(1));
    }
    
    @Test
    public void assertListenCommitWithoutBegin() throws SQLException {
        SagaTransactionEvent event = new SagaTransactionEvent(TransactionOperationType.COMMIT);
        eventBus.post(event);
        verify(sagaTransactionManager, never()).commit(event);
    }
    
    @Test
    public void assertListenCommitWithBegin() throws SQLException {
        eventBus.post(new SagaTransactionEvent(TransactionOperationType.BEGIN));
        assertThat(builderMap.size(), is(1));
        SagaTransactionEvent event = new SagaTransactionEvent(TransactionOperationType.COMMIT);
        eventBus.post(event);
        verify(sagaTransactionManager).commit(event);
        assertThat(builderMap.size(), is(0));
    }
    
    @Test
    public void assertListenRollback() throws SQLException {
        SagaTransactionEvent event = new SagaTransactionEvent(TransactionOperationType.ROLLBACK);
        eventBus.post(event);
        verify(sagaTransactionManager).rollback(event);
        assertThat(builderMap.size(), is(0));
    }
    
    @Test
    public void assertListenSagaSQLExecutionEvent() throws NoSuchFieldException, IllegalAccessException {
        builderMap.put(id, new SagaDefinitionBuilder());
        SagaSQLExecutionEvent event = new SagaSQLExecutionEvent("ds", new SQLUnit("", null), Collections.emptyList(), id);
        event.setExecuteSuccess();
        eventBus.post(event);
        assertThat(getRequestLength(), is(1));
        assertThat(getParentsLength(), is(0));
        event = new SagaSQLExecutionEvent("", null, null, id);
        eventBus.post(event);
        assertThat(getParentsLength(), is(1));
    }
    
    private int getRequestLength() throws NoSuchFieldException, IllegalAccessException {
        SagaDefinitionBuilder builder = builderMap.get(id);
        Field requests = SagaDefinitionBuilder.class.getDeclaredField("requests");
        requests.setAccessible(true);
        List<SagaRequest> list = (List<SagaRequest>) requests.get(builder);
        return list.size();
    }
    
    private int getParentsLength() throws NoSuchFieldException, IllegalAccessException {
        SagaDefinitionBuilder builder = builderMap.get(id);
        Field parents = SagaDefinitionBuilder.class.getDeclaredField("parents");
        parents.setAccessible(true);
        String[] array = (String[]) parents.get(builder);
        return array.length;
    }
}
