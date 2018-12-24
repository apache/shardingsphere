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
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.transaction.core.internal.context.SagaSQLExecutionContext;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import io.shardingsphere.transaction.saga.revert.RevertResult;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingTransportFactory;
import io.shardingsphere.transaction.saga.servicecomb.transport.ConnectionMapSQLTransport;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga SQL execution event handler.
 *
 * @author yangyi
 */
@Slf4j
public final class SagaSQLExecutionContextHandler {
    
    private final Map<String, String> transactionIdToLogicSQLIdMap = new ConcurrentHashMap<>();
    
    private final Map<String, String> logicSQLIdToTransactionIdMap = new ConcurrentHashMap<>();
    
    private SagaTransactionManager transactionManager;
    
    /**
     * Handle saga SQL execution event.
     *
     * @param sqlExecutionEvent saga SQL execution event
     */
    public void handle(final SagaSQLExecutionContext sqlExecutionEvent) {
        checkTransactionManager();
        if (sqlExecutionEvent.isNewLogicSQL()) {
            transactionManager.getSagaDefinitionBuilder(transactionManager.getTransactionId()).switchParents();
            if (null != transactionIdToLogicSQLIdMap.get(transactionManager.getTransactionId())) {
                logicSQLIdToTransactionIdMap.remove(transactionIdToLogicSQLIdMap.get(transactionManager.getTransactionId()));
            }
            logicSQLIdToTransactionIdMap.put(sqlExecutionEvent.getLogicSQLId(), transactionManager.getTransactionId());
            transactionIdToLogicSQLIdMap.put(transactionManager.getTransactionId(), sqlExecutionEvent.getLogicSQLId());
        } else {
            //TODO generate revert sql by sql and params in event
            try {
                String transactionId = logicSQLIdToTransactionIdMap.get(sqlExecutionEvent.getLogicSQLId());
                StatementExecuteUnit executeUnit = sqlExecutionEvent.getExecuteUnit();
                RouteUnit routeUnit = executeUnit.getRouteUnit();
                ((ConnectionMapSQLTransport) ShardingTransportFactory.getInstance().getTransportByTransactionId(transactionId)).cacheStatement(executeUnit);
                RevertResult result = transactionManager.getReverEngine(transactionId).revert(routeUnit.getDataSourceName(), routeUnit.getSqlUnit().getSql(), routeUnit.getSqlUnit().getParameterSets());
                transactionManager.getSagaDefinitionBuilder(transactionId).addChildRequest(sqlExecutionEvent.getId(), routeUnit.getDataSourceName(),
                    routeUnit.getSqlUnit().getSql(), copyList(routeUnit.getSqlUnit().getParameterSets()), result.getRevertSQL(), result.getRevertSQLParams());
            } catch (SQLException e) {
                throw new ShardingException("Failed to revert SQL", e);
            }
        }
    }
    
    /**
     * Clean all cache about this transaction.
     */
    public void clean() {
        checkTransactionManager();
        logicSQLIdToTransactionIdMap.remove(transactionIdToLogicSQLIdMap.remove(transactionManager.getTransactionId()));
    }
    
    private void checkTransactionManager() {
        if (null == transactionManager) {
            transactionManager = SagaTransactionManager.getInstance();
        }
    }
    
    private List<List<Object>> copyList(final List<List<Object>> origin) {
        List<List<Object>> result = new ArrayList<>();
        for (List<Object> each : origin) {
            result.add(Lists.newArrayList(each));
        }
        return result;
    }
}
