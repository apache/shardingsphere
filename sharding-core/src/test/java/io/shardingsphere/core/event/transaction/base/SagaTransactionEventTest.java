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

package io.shardingsphere.core.event.transaction.base;

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SagaTransactionEventTest {
    
    @Mock
    private Map<String, DataSource> dataSourceMap;
    
    @Mock
    private SagaConfiguration sagaConfiguration;
    
    @Test
    public void assertCreateBeginSagaTransactionEvent() {
        SagaTransactionEvent sagaTransactionEvent = SagaTransactionEvent.createBeginSagaTransactionEvent(dataSourceMap, sagaConfiguration);
        assertThat(sagaTransactionEvent.getDataSourceMap(), is(dataSourceMap));
        assertThat(sagaTransactionEvent.getSagaConfiguration(), is(sagaConfiguration));
        assertThat(sagaTransactionEvent.getOperationType(), is(TransactionOperationType.BEGIN));
        assertNull(sagaTransactionEvent.getSagaSQLExecutionEvent());
    }
    
    @Test
    public void assertCreateCommitSagaTransactionEvent() {
        SagaTransactionEvent sagaTransactionEvent = SagaTransactionEvent.createCommitSagaTransactionEvent(sagaConfiguration);
        assertThat(sagaTransactionEvent.getSagaConfiguration(), is(sagaConfiguration));
        assertThat(sagaTransactionEvent.getOperationType(), is(TransactionOperationType.COMMIT));
        assertNull(sagaTransactionEvent.getDataSourceMap());
        assertNull(sagaTransactionEvent.getSagaSQLExecutionEvent());
    }
    
    @Test
    public void assertCreateRollbackSagaTransactionEvent() {
        SagaTransactionEvent sagaTransactionEvent = SagaTransactionEvent.createRollbackSagaTransactionEvent(sagaConfiguration);
        assertThat(sagaTransactionEvent.getSagaConfiguration(), is(sagaConfiguration));
        assertThat(sagaTransactionEvent.getOperationType(), is(TransactionOperationType.ROLLBACK));
        assertNull(sagaTransactionEvent.getDataSourceMap());
        assertNull(sagaTransactionEvent.getSagaSQLExecutionEvent());
    }
    
    @Test
    public void assertCreateExecutionSagaTransactionEvent() {
        SagaSQLExecutionEvent sagaSQLExecutionEvent = new SagaSQLExecutionEvent(null, "test", true);
        SagaTransactionEvent sagaTransactionEvent = SagaTransactionEvent.createExecutionSagaTransactionEvent(sagaSQLExecutionEvent);
        assertNull(sagaTransactionEvent.getOperationType());
        assertNull(sagaTransactionEvent.getDataSourceMap());
        assertNull(sagaTransactionEvent.getSagaConfiguration());
        assertThat(sagaTransactionEvent.getSagaSQLExecutionEvent(), is(sagaSQLExecutionEvent));
        assertTrue(sagaTransactionEvent.isExecutionEvent());
    }
}