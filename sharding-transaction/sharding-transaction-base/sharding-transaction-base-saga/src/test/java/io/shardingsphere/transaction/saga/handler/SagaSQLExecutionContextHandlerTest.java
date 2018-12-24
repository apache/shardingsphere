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

import com.google.common.collect.Lists;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.core.internal.context.SagaSQLExecutionContext;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import io.shardingsphere.transaction.saga.revert.RevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertResult;
import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SagaSQLExecutionContextHandlerTest {
    
    @Mock
    private SagaTransactionManager transactionManager;
    
    @Mock
    private SagaDefinitionBuilder sagaDefinitionBuilder;
    
    @Mock
    private RevertEngine revertEngine;
    
    private final SagaSQLExecutionContextHandler sagaSQLExecutionContextHandler = new SagaSQLExecutionContextHandler();
    
    private final String txId = "1";
    
    private Map<String, String> transactionIdToLogicSQLIdMap;
    
    private Map<String, String> logicSQLIdToTransactionIdMap;
    
    @Before
    public void setUp() throws Exception {
        when(transactionManager.getTransactionId()).thenReturn(txId);
//        when(transactionManager.getSagaDefinitionBuilder(txId)).thenReturn(sagaDefinitionBuilder);
//        when(transactionManager.getReverEngine(txId)).thenReturn(revertEngine);
        reflectSagaSQLExecutionContextHandler();
    }
    
//    @Test
    public void assertHandleSQLExecutionEvent() throws SQLException {
        String database = "test";
        String logicId = "logicId-1";
        String sql = "Select 1";
        List<List<Object>> params = Lists.newArrayList();
        params.add(Lists.newArrayList());
        RevertResult revertResult = mock(RevertResult.class);
        when(revertResult.getRevertSQL()).thenReturn(sql);
        when(revertEngine.revert(database, sql, params)).thenReturn(revertResult);
        RouteUnit routeUnit = new RouteUnit(database, new SQLUnit(sql, params));
        SagaSQLExecutionContext context = new SagaSQLExecutionContext(null, logicId, true);
        sagaSQLExecutionContextHandler.handle(context);
        verify(sagaDefinitionBuilder).switchParents();
        assertThat(transactionIdToLogicSQLIdMap.size(), is(1));
        assertThat(transactionIdToLogicSQLIdMap.get(txId), is(logicId));
        assertThat(logicSQLIdToTransactionIdMap.size(), is(1));
        assertThat(logicSQLIdToTransactionIdMap.get(logicId), is(txId));
//        context = new SagaSQLExecutionContext(routeUnit, logicId, false);
        sagaSQLExecutionContextHandler.handle(context);
        verify(revertEngine).revert(database, sql, params);
        verify(sagaDefinitionBuilder).addChildRequest(context.getId(), database, sql, params, sql, revertResult.getRevertSQLParams());
        logicId = "logicId-2";
        context = new SagaSQLExecutionContext(null, logicId, true);
        sagaSQLExecutionContextHandler.handle(context);
        verify(sagaDefinitionBuilder, new Times(2)).switchParents();
        assertThat(transactionIdToLogicSQLIdMap.size(), is(1));
        assertThat(transactionIdToLogicSQLIdMap.get(txId), is(logicId));
        assertThat(logicSQLIdToTransactionIdMap.size(), is(1));
        assertThat(logicSQLIdToTransactionIdMap.get(logicId), is(txId));
    }
    
//    @Test(expected = ShardingException.class)
    public void assertGetRevertFailure() throws SQLException {
        String database = "test";
        String logicId = "logicId-revert-failure";
        String sql = "Select 1";
        List<List<Object>> params = Lists.newArrayList();
        when(revertEngine.revert(database, sql, params)).thenThrow(new SQLException("test"));
        RouteUnit routeUnit = new RouteUnit(database, new SQLUnit(sql, params));
        SagaSQLExecutionContext context = new SagaSQLExecutionContext(null, logicId, true);
        sagaSQLExecutionContextHandler.handle(context);
        verify(sagaDefinitionBuilder).switchParents();
        assertThat(transactionIdToLogicSQLIdMap.size(), is(1));
        assertThat(transactionIdToLogicSQLIdMap.get(txId), is(logicId));
        assertThat(logicSQLIdToTransactionIdMap.size(), is(1));
        assertThat(logicSQLIdToTransactionIdMap.get(logicId), is(txId));
//        context = new SagaSQLExecutionContext(routeUnit, logicId, false);
        sagaSQLExecutionContextHandler.handle(context);
    }
    
    @Test
    public void assertClean() {
        String logicId = "logicId";
        transactionIdToLogicSQLIdMap.put(txId, logicId);
        logicSQLIdToTransactionIdMap.put(logicId, txId);
        sagaSQLExecutionContextHandler.clean();
        assertThat(transactionIdToLogicSQLIdMap.size(), is(0));
        assertThat(logicSQLIdToTransactionIdMap.size(), is(0));
    }
    
    private void reflectSagaSQLExecutionContextHandler() throws NoSuchFieldException, IllegalAccessException {
        Field transactionManagerField = SagaSQLExecutionContextHandler.class.getDeclaredField("transactionManager");
        transactionManagerField.setAccessible(true);
        transactionManagerField.set(sagaSQLExecutionContextHandler, transactionManager);
        Field transactionIdToLogicSQLIdMapField = SagaSQLExecutionContextHandler.class.getDeclaredField("transactionIdToLogicSQLIdMap");
        transactionIdToLogicSQLIdMapField.setAccessible(true);
        transactionIdToLogicSQLIdMap = (Map<String, String>) transactionIdToLogicSQLIdMapField.get(sagaSQLExecutionContextHandler);
        Field logicSQLIdToTransactionIdMapField = SagaSQLExecutionContextHandler.class.getDeclaredField("logicSQLIdToTransactionIdMap");
        logicSQLIdToTransactionIdMapField.setAccessible(true);
        logicSQLIdToTransactionIdMap = (Map<String, String>) logicSQLIdToTransactionIdMapField.get(sagaSQLExecutionContextHandler);
    }
}