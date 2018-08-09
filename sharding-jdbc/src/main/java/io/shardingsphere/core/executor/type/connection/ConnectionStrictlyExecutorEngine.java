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

package io.shardingsphere.core.executor.type.connection;

import com.google.common.util.concurrent.ListenableFuture;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.BaseStatementUnit;
import io.shardingsphere.core.executor.ExecuteCallback;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.threadlocal.ExecutorExceptionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Connection strictly execute engine.
 *
 * @author panjuan
 */
public final class ConnectionStrictlyExecutorEngine extends ExecutorEngine {
    
    public ConnectionStrictlyExecutorEngine(final int executorSize) {
        super(executorSize);
    }
    
    @Override
    protected <T> List<T> getExecuteResults(final SQLType sqlType, final Collection<? extends BaseStatementUnit> baseStatementUnits, final ExecuteCallback<T> executeCallback) throws Exception {
        Map<String, Collection<BaseStatementUnit>> baseStatementUnitGroups = getBaseStatementUnitGroups(baseStatementUnits);
        Collection<T> firstOutputs = syncExecute(sqlType, baseStatementUnitGroups.remove(baseStatementUnitGroups.keySet().iterator().next()), executeCallback);
        Collection<ListenableFuture<Collection<T>>> restResultFutures = asyncExecute(sqlType, baseStatementUnitGroups, executeCallback);
        return getResultList(firstOutputs, restResultFutures);
    }
    
    private Map<String, Collection<BaseStatementUnit>> getBaseStatementUnitGroups(final Collection<? extends BaseStatementUnit> baseStatementUnits) {
        Map<String, Collection<BaseStatementUnit>> result = new LinkedHashMap<>(baseStatementUnits.size(), 1);
        for (BaseStatementUnit each : baseStatementUnits) {
            String dataSourceName = each.getSqlExecutionUnit().getDataSource();
            if (!result.keySet().contains(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<BaseStatementUnit>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
    
    private <T> Collection<ListenableFuture<Collection<T>>> asyncExecute(
            final SQLType sqlType, final Map<String, Collection<BaseStatementUnit>> baseStatementUnitGroups, final ExecuteCallback<T> executeCallback) {
        Collection<ListenableFuture<Collection<T>>> result = new ArrayList<>(baseStatementUnitGroups.size());
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        for (Map.Entry<String, Collection<BaseStatementUnit>> entry : baseStatementUnitGroups.entrySet()) {
            final Collection<BaseStatementUnit> baseStatementUnits = entry.getValue();
            result.add(getExecutorService().submit(new Callable<Collection<T>>() {
                @Override
                public Collection<T> call() throws Exception {
                    Collection<T> result = new LinkedList<>();
                    for (BaseStatementUnit each : baseStatementUnits) {
                        result.add(executeInternal(sqlType, each, executeCallback, isExceptionThrown, dataMap));
                    }
                    return result;
                }
            }));
        }
        return result;
    }
    
    private <T> Collection<T> syncExecute(final SQLType sqlType, final Collection<? extends BaseStatementUnit> baseStatementUnits, final ExecuteCallback<T> executeCallback) throws Exception {
        Collection<T> result = new LinkedList<>();
        for (BaseStatementUnit each : baseStatementUnits) {
            result.add(executeInternal(sqlType, each, executeCallback, ExecutorExceptionHandler.isExceptionThrown(), ExecutorDataMap.getDataMap()));
        }
        return result;
    }
    
    private <T> List<T> getResultList(final Collection<T> firstOutputs, final Collection<ListenableFuture<Collection<T>>> restResultFutures) throws ExecutionException, InterruptedException {
        List<T> result = new LinkedList<>();
        result.addAll(firstOutputs);
        for (ListenableFuture<Collection<T>> each : restResultFutures) {
            result.addAll(each.get());
        }
        return result;
    }
}
