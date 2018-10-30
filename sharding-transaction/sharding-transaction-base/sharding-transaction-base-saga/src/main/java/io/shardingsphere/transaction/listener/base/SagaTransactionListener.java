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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.event.transaction.base.SagaSQLExecutionEvent;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.transaction.listener.ShardingTransactionListenerAdapter;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;
import io.shardingsphere.transaction.manager.base.SagaTransactionManager;
import io.shardingsphere.transaction.manager.base.servicecomb.SagaDefinitionBuilder;
import io.shardingsphere.transaction.revert.RevertEngine;
import io.shardingsphere.transaction.revert.RevertEngineHolder;
import io.shardingsphere.transaction.revert.RevertResult;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Status;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga transaction listener.
 *
 * @author yangyi
 */
@Slf4j
public final class SagaTransactionListener extends ShardingTransactionListenerAdapter<SagaTransactionEvent> {
    
    private final BASETransactionManager<SagaTransactionEvent> transactionManager = SagaTransactionManager.getInstance();
    
    private final Map<String, SagaDefinitionBuilder> sagaDefinitionBuilderMap = new ConcurrentHashMap<>();
    
    private final Map<String, RevertEngine> revertEngineMap = new ConcurrentHashMap<>();
    
    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void listen(final SagaTransactionEvent transactionEvent) throws SQLException {
        switch (transactionEvent.getOperationType()) {
            case COMMIT:
                try {
                    transactionEvent.setSagaJson(sagaDefinitionBuilderMap.remove(transactionManager.getTransactionId()).build());
                    revertEngineMap.remove(transactionManager.getTransactionId());
                    doTransaction(transactionManager, transactionEvent);
                } catch (JsonProcessingException e) {
                    // shouldn't really happen, but is declared as possibility so:
                    log.error("saga transaction", transactionManager.getTransactionId(), "commit failed, caused by json build exception: ", e);
                    return;
                }
                break;
            case ROLLBACK:
                sagaDefinitionBuilderMap.remove(transactionManager.getTransactionId());
                revertEngineMap.remove(transactionManager.getTransactionId());
                doTransaction(transactionManager, transactionEvent);
                break;
            case BEGIN:
                if (transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    doTransaction(transactionManager, transactionEvent);
                    sagaDefinitionBuilderMap.put(transactionManager.getTransactionId(), new SagaDefinitionBuilder());
                    revertEngineMap.put(transactionManager.getTransactionId(), RevertEngineHolder.getInstance().getRevertEngine());
                }
                break;
            default:
        }
    }
    
    /**
     * listen Saga Sql execution event.
     *
     * @param sqlExecutionEvent saga sql execution event
     * @throws SQLException SQL exception
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listenSQLExecutionEvent(final SagaSQLExecutionEvent sqlExecutionEvent) throws SQLException {
        switch (sqlExecutionEvent.getEventType()) {
            case BEFORE_EXECUTE:
                sagaDefinitionBuilderMap.get(sqlExecutionEvent.getTransactionId()).switchParents();
                break;
            case EXECUTE_SUCCESS:
                //TODO generate revert sql by sql and params in event
                RevertResult result = revertEngineMap.get(sqlExecutionEvent.getTransactionId()).revert(
                        sqlExecutionEvent.getRouteUnit().getDataSourceName(),
                        sqlExecutionEvent.getRouteUnit().getSqlUnit().getSql(),
                        sqlExecutionEvent.getRouteUnit().getSqlUnit().getParameterSets());
                sagaDefinitionBuilderMap.get(sqlExecutionEvent.getTransactionId()).addChildRequest(
                        sqlExecutionEvent.getId(),
                        sqlExecutionEvent.getRouteUnit().getDataSourceName(),
                        sqlExecutionEvent.getRouteUnit().getSqlUnit().getSql(),
                        copyList(sqlExecutionEvent.getRouteUnit().getSqlUnit().getParameterSets()),
                        result.getRevertSQL(),
                        result.getRevertSQLParams());
                break;
            case EXECUTE_FAILURE:
            default:
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
