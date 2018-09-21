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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SagaTransactionManagerTest {
    
    private final SagaTransactionManager transactionManager = new SagaTransactionManager();
    
    @Mock
    private SagaExecutionComponent coordinator;
    
    private final String sagaJson = "{}";
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        when(coordinator.run(sagaJson)).thenReturn(new SuccessfulSagaResponse("{}"));
        Field field = SagaTransactionManager.class.getDeclaredField("coordinator");
        field.setAccessible(true);
        field.set(transactionManager, coordinator);
    }
    
    @Test
    public void assertBegin() {
        transactionManager.begin(new SagaTransactionEvent(TransactionOperationType.BEGIN));
        assertThat(36 == transactionManager.getTransactionId().length(), is(true));
    }
    
    @Test
    public void assertCommit() {
        SagaTransactionEvent event = new SagaTransactionEvent(TransactionOperationType.COMMIT);
        event.setSagaJson(sagaJson);
        transactionManager.commit(event);
        assertThat(null == transactionManager.getTransactionId(), is(true));
    }
    
    @Test
    public void assertRollback() {
        transactionManager.rollback(new SagaTransactionEvent(TransactionOperationType.ROLLBACK));
        assertThat( null == transactionManager.getTransactionId(), is(true));
    }
    
    @Test
    public void assertGetStatus() {
        transactionManager.begin(new SagaTransactionEvent(TransactionOperationType.BEGIN));
        assertThat(transactionManager.getStatus(), is(Status.STATUS_ACTIVE));
        transactionManager.rollback(new SagaTransactionEvent(TransactionOperationType.ROLLBACK));
        assertThat(transactionManager.getStatus(), is(Status.STATUS_NO_TRANSACTION));
    }
}
