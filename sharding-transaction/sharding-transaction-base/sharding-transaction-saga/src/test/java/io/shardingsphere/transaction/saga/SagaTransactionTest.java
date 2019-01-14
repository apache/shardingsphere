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

package io.shardingsphere.transaction.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shardingsphere.transaction.saga.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.config.SagaConfiguration;
import io.shardingsphere.transaction.saga.constant.SagaRecoveryPolicy;
import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;

import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SagaTransactionTest {
    
    private SagaTransaction sagaTransaction;
    
    @Before
    public void setUp() {
        sagaTransaction = new SagaTransaction(new SagaConfiguration());
    }
    
    @Test
    public void assertNextLogicSQL() {
        sagaTransaction.nextLogicSQL();
        assertNotNull(sagaTransaction.getCurrentLogicSQL());
        assertEquals(sagaTransaction.getLogicSQLs().size(), 1);
        sagaTransaction.nextLogicSQL();
        assertEquals(sagaTransaction.getLogicSQLs().size(), 2);
    }
    
    @Test
    public void assertRecordResult() {
        sagaTransaction.nextLogicSQL();
        SagaSubTransaction sagaSubTransaction = mock(SagaSubTransaction.class);
        sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.EXECUTING);
        assertEquals(sagaTransaction.getExecutionResultMap().size(), 1);
        assertTrue(sagaTransaction.getExecutionResultMap().containsKey(sagaSubTransaction));
        assertEquals(sagaTransaction.getExecutionResultMap().get(sagaSubTransaction), ExecutionResult.EXECUTING);
        assertFalse(sagaTransaction.isContainException());
        assertEquals(sagaTransaction.getCurrentLogicSQL().size(), 1);
        sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.SUCCESS);
        assertEquals(sagaTransaction.getExecutionResultMap().size(), 1);
        assertTrue(sagaTransaction.getExecutionResultMap().containsKey(sagaSubTransaction));
        assertEquals(sagaTransaction.getExecutionResultMap().get(sagaSubTransaction), ExecutionResult.SUCCESS);
        assertFalse(sagaTransaction.isContainException());
        assertEquals(sagaTransaction.getCurrentLogicSQL().size(), 1);
        sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.FAILURE);
        assertEquals(sagaTransaction.getExecutionResultMap().size(), 1);
        assertTrue(sagaTransaction.getExecutionResultMap().containsKey(sagaSubTransaction));
        assertEquals(sagaTransaction.getExecutionResultMap().get(sagaSubTransaction), ExecutionResult.FAILURE);
        assertTrue(sagaTransaction.isContainException());
        assertEquals(sagaTransaction.getCurrentLogicSQL().size(), 1);
    }
    
    @Test
    public void assertGetSagaDefinitionBuilder() throws IOException {
        sagaTransaction.nextLogicSQL();
        SagaSubTransaction sagaSubTransaction = mock(SagaSubTransaction.class);
        sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.EXECUTING);
        sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.SUCCESS);
        SagaDefinitionBuilder builder = sagaTransaction.getSagaDefinitionBuilder();
        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        Map<String, Object> sagaDefinitionMap = jacksonObjectMapper.readValue(builder.build(), Map.class);
        assertEquals(sagaDefinitionMap.get("policy"), SagaRecoveryPolicy.FORWARD.getName());
        assertThat(sagaDefinitionMap.get("requests"), instanceOf(List.class));
        List<Object> requests = (List<Object>) sagaDefinitionMap.get("requests");
        assertEquals(requests.size(), 1);
        assertThat(requests.get(0), instanceOf(Map.class));
        Map<String, Object> request = (Map<String, Object>) requests.get(0);
        assertEquals(request.size(), 7);
        assertTrue(request.containsKey("id"));
        assertTrue(request.containsKey("datasource"));
        assertTrue(request.containsKey("type"));
        assertTrue(request.containsKey("transaction"));
        assertTrue(request.containsKey("compensation"));
        assertTrue(request.containsKey("parents"));
        assertTrue(request.containsKey("failRetryDelayMilliseconds"));
    }
}