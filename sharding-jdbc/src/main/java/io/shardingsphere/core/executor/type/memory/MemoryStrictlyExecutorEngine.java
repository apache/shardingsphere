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

package io.shardingsphere.core.executor.type.memory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.BaseStatementUnit;
import io.shardingsphere.core.executor.ExecuteCallback;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.threadlocal.ExecutorExceptionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Memory strictly execute engine.
 *
 * @author panjuan
 */
public final class MemoryStrictlyExecutorEngine extends ExecutorEngine {
    
    public MemoryStrictlyExecutorEngine(final int executorSize) {
        super(executorSize);
    }
    
    @Override
    protected <T> List<T> getExecuteResults(final SQLType sqlType, final Collection<? extends BaseStatementUnit> baseStatementUnits, final ExecuteCallback<T> executeCallback) throws Exception {
        Iterator<? extends BaseStatementUnit> iterator = baseStatementUnits.iterator();
        T firstOutput = syncExecute(sqlType, iterator.next(), executeCallback);
        Collection<ListenableFuture<T>> restFutures = asyncExecute(sqlType, Lists.newArrayList(iterator), executeCallback);
        return getResultList(firstOutput, restFutures);
    }
    
    private <T> Collection<ListenableFuture<T>> asyncExecute(
            final SQLType sqlType, final Collection<BaseStatementUnit> baseStatementUnits, final ExecuteCallback<T> executeCallback) {
        List<ListenableFuture<T>> result = new ArrayList<>(baseStatementUnits.size());
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        for (final BaseStatementUnit each : baseStatementUnits) {
            result.add(getExecutorService().submit(new Callable<T>() {
                
                @Override
                public T call() throws Exception {
                    return executeInternal(sqlType, each, executeCallback, isExceptionThrown, dataMap);
                }
            }));
        }
        return result;
    }
    
    private <T> T syncExecute(final SQLType sqlType, final BaseStatementUnit baseStatementUnit, final ExecuteCallback<T> executeCallback) throws Exception {
        return executeInternal(sqlType, baseStatementUnit, executeCallback, ExecutorExceptionHandler.isExceptionThrown(), ExecutorDataMap.getDataMap());
    }
    
    private <T> List<T> getResultList(final T firstOutput, final Collection<ListenableFuture<T>> restResultFutures) throws ExecutionException, InterruptedException {
        List<T> result = new LinkedList<>();
        result.add(firstOutput);
        for (ListenableFuture<T> each : restResultFutures) {
            result.add(each.get());
        }
        return result;
    }
}

