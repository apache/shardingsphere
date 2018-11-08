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

package io.shardingsphere.transaction.base.manager;

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import org.apache.servicecomb.saga.core.SuccessfulSagaResponse;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.transaction.Status;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SagaTransactionManagerTest {
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    private final String sagaJson = "{}";
    
    private static SagaConfiguration config = new SagaConfiguration();
    
    private static SagaConfiguration config2 = new SagaConfiguration();
    
    @Mock
    private SagaExecutionComponent coordinator;
    
    private Map<String, SagaExecutionComponent> sagaCaches;
    
    private Map<String, ExecutorService> executorCaches;
    
    private Method getSagaExecutionComponentMethod;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        when(coordinator.run(sagaJson)).thenReturn(new SuccessfulSagaResponse("{}"));
        Field sagaCachesField = SagaTransactionManager.class.getDeclaredField("sagaCaches");
        Field executorCachesField = SagaTransactionManager.class.getDeclaredField("executorCaches");
        sagaCachesField.setAccessible(true);
        executorCachesField.setAccessible(true);
        sagaCaches = (Map<String, SagaExecutionComponent>) sagaCachesField.get(transactionManager);
        executorCaches = (Map<String, ExecutorService>) executorCachesField.get(transactionManager);
        sagaCaches.clear();
        executorCaches.clear();
    
        getSagaExecutionComponentMethod = SagaTransactionManager.class.getDeclaredMethod("getSagaExecutionComponent", SagaConfiguration.class);
        getSagaExecutionComponentMethod.setAccessible(true);
    }
    
    
    @Test
    public void assertGetSagaExecutionComponentWithSameConfig() throws InvocationTargetException, IllegalAccessException {
        assertThat(sagaCaches.size(), is(0));
        assertThat(executorCaches.size(), is(0));
        SagaExecutionComponent first = (SagaExecutionComponent) getSagaExecutionComponentMethod.invoke(transactionManager, config);
        assertThat(sagaCaches.size(), is(1));
        assertThat(executorCaches.size(), is(1));
        SagaExecutionComponent second = (SagaExecutionComponent) getSagaExecutionComponentMethod.invoke(transactionManager, config);
        assertTrue(first == second);
    }
    
    @Test
    public void assertGetSagaExecutionComponentWithDiffConfig() throws InvocationTargetException, IllegalAccessException {
        assertThat(sagaCaches.size(), is(0));
        assertThat(executorCaches.size(), is(0));
        SagaExecutionComponent first = (SagaExecutionComponent) getSagaExecutionComponentMethod.invoke(transactionManager, config);
        assertThat(sagaCaches.size(), is(1));
        assertThat(executorCaches.size(), is(1));
        SagaExecutionComponent second = (SagaExecutionComponent) getSagaExecutionComponentMethod.invoke(transactionManager, config2);
        assertTrue(first != second);
    }
    
    @Test
    public void assertRemoveSagaExecutionComponent() throws InvocationTargetException, IllegalAccessException {
        SagaExecutionComponent first = (SagaExecutionComponent) getSagaExecutionComponentMethod.invoke(transactionManager, config);
        transactionManager.removeSagaExecutionComponent(config);
        assertThat(sagaCaches.size(), is(0));
        assertThat(executorCaches.size(), is(0));
    }
    
    @Test
    public void assertBegin() {
        transactionManager.begin(new SagaTransactionEvent(TransactionOperationType.BEGIN, config));
        assertThat(36 == transactionManager.getTransactionId().length(), is(true));
    }
    
    @Test
    public void assertCommit() {
        sagaCaches.put(config.getAlias(), coordinator);
        SagaTransactionEvent event = new SagaTransactionEvent(TransactionOperationType.COMMIT, config);
        event.setSagaJson(sagaJson);
        transactionManager.commit(event);
        assertThat(null == transactionManager.getTransactionId(), is(true));
        verify(coordinator).run(sagaJson);
    }
    
    @Test
    public void assertRollback() {
        transactionManager.rollback(new SagaTransactionEvent(TransactionOperationType.ROLLBACK, config));
        assertThat(null == transactionManager.getTransactionId(), is(true));
    }
    
    @Test
    public void assertGetStatus() {
        transactionManager.begin(new SagaTransactionEvent(TransactionOperationType.BEGIN, config));
        assertThat(transactionManager.getStatus(), is(Status.STATUS_ACTIVE));
        transactionManager.rollback(new SagaTransactionEvent(TransactionOperationType.ROLLBACK, config));
        assertThat(transactionManager.getStatus(), is(Status.STATUS_NO_TRANSACTION));
    }
}
