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

import io.shardingsphere.core.executor.ShardingExecuteDataMap;
import io.shardingsphere.transaction.saga.config.SagaConfiguration;
import io.shardingsphere.transaction.saga.SagaTransaction;
import io.shardingsphere.transaction.saga.persistence.SagaPersistence;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingSQLTransport;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingTransportFactory;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.transaction.Status;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SagaTransactionManagerTest {
    
    @Mock
    private SagaResourceManager resourceManager;
    
    @Mock
    private SagaPersistence sagaPersistence;
    
    @Mock
    private SagaExecutionComponent sagaExecutionComponent;
    
    private final String transactionKey = "transaction";
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    private final SagaConfiguration config = new SagaConfiguration();
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        when(resourceManager.getSagaConfiguration()).thenReturn(config);
        when(resourceManager.getSagaExecutionComponent()).thenReturn(sagaExecutionComponent);
        when(resourceManager.getSagaPersistence()).thenReturn(sagaPersistence);
        Field resourceManagerField = SagaTransactionManager.class.getDeclaredField("resourceManager");
        resourceManagerField.setAccessible(true);
        resourceManagerField.set(transactionManager, resourceManager);
    }
    
    @Test
    public void assertBegin() {
        transactionManager.begin();
        verify(resourceManager).getSagaConfiguration();
        assertNotNull(transactionManager.getTransaction());
        assertTrue(ShardingExecuteDataMap.getDataMap().containsKey(transactionKey));
        assertThat(ShardingExecuteDataMap.getDataMap().get(transactionKey), instanceOf(SagaTransaction.class));
        assertThat(ShardingTransportFactory.getInstance().getTransport(), instanceOf(ShardingSQLTransport.class));
    }
    
    @Test
    public void assertCommitWithoutBegin() {
        transactionManager.commit();
        verify(sagaExecutionComponent, never()).run(anyString());
        assertNull(transactionManager.getTransaction());
        assertEquals(ShardingExecuteDataMap.getDataMap().size(), 0);
        assertNull(ShardingTransportFactory.getInstance().getTransport());
    }
    
    @Test
    public void assertCommitWithBegin() throws NoSuchFieldException, IllegalAccessException {
        mockWithoutException();
        mockWithException();
    }
    
    @Test
    public void assertRollbackWithoutBegin() {
        transactionManager.rollback();
        assertNull(transactionManager.getTransaction());
        assertEquals(ShardingExecuteDataMap.getDataMap().size(), 0);
        assertNull(ShardingTransportFactory.getInstance().getTransport());
    }
    
    @Test
    public void assertRollbackWithBegin() {
        transactionManager.begin();
        transactionManager.rollback();
        assertNull(transactionManager.getTransaction());
        assertEquals(ShardingExecuteDataMap.getDataMap().size(), 0);
        assertNull(ShardingTransportFactory.getInstance().getTransport());
    }
    
    @Test
    public void assertGetStatus() {
        transactionManager.begin();
        assertThat(transactionManager.getStatus(), is(Status.STATUS_ACTIVE));
        transactionManager.rollback();
        assertThat(transactionManager.getStatus(), is(Status.STATUS_NO_TRANSACTION));
    }
    
    private void mockWithoutException() {
        transactionManager.begin();
        transactionManager.commit();
        verify(sagaExecutionComponent, never()).run(anyString());
        assertNull(transactionManager.getTransaction());
        assertEquals(ShardingExecuteDataMap.getDataMap().size(), 0);
        assertNull(ShardingTransportFactory.getInstance().getTransport());
    }
    
    private void mockWithException() throws NoSuchFieldException, IllegalAccessException {
        transactionManager.begin();
        Field containExceptionField = SagaTransaction.class.getDeclaredField("containException");
        containExceptionField.setAccessible(true);
        containExceptionField.set(ShardingExecuteDataMap.getDataMap().get(transactionKey), true);
        transactionManager.commit();
        verify(resourceManager).getSagaExecutionComponent();
        verify(sagaExecutionComponent).run(anyString());
        assertNull(transactionManager.getTransaction());
        assertEquals(ShardingExecuteDataMap.getDataMap().size(), 0);
        assertNull(ShardingTransportFactory.getInstance().getTransport());
    }
}
