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

package io.shardingsphere.transaction.base.handler;

import com.google.common.collect.Lists;
import io.shardingsphere.core.event.transaction.base.SagaSQLExecutionEvent;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.transaction.base.manager.SagaTransactionManager;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;
import io.shardingsphere.transaction.revert.RevertResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Saga SQL execution event handler.
 *
 * @author yangyi
 */
@Slf4j
public class SagaSQLExecutionEventHandler {
    
    private final BASETransactionManager<SagaTransactionEvent> transactionManager = SagaTransactionManager.getInstance();
    
    /**
     * Handle saga SQL execution event.
     *
     * @param sqlExecutionEvent saga SQL execution event
     */
    public void handleSQLExecutionEvent(final SagaSQLExecutionEvent sqlExecutionEvent) {
        if (sqlExecutionEvent.isNewLogicSQL()) {
            sagaDefinitionBuilderMap.get(sqlExecutionEvent.getTransactionId()).switchParents();
        } else {
            //TODO generate revert sql by sql and params in event
            RevertResult result = revertEngineMap.get(sqlExecutionEvent.getTransactionId()).revert(sqlExecutionEvent.getRouteUnit().getDataSourceName(),
                sqlExecutionEvent.getRouteUnit().getSqlUnit().getSql(), sqlExecutionEvent.getRouteUnit().getSqlUnit().getParameterSets());
            sagaDefinitionBuilderMap.get(sqlExecutionEvent.getTransactionId()).addChildRequest(sqlExecutionEvent.getId(), sqlExecutionEvent.getRouteUnit().getDataSourceName(),
                sqlExecutionEvent.getRouteUnit().getSqlUnit().getSql(), copyList(sqlExecutionEvent.getRouteUnit().getSqlUnit().getParameterSets()),
                result.getRevertSQL(), result.getRevertSQLParams());
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
