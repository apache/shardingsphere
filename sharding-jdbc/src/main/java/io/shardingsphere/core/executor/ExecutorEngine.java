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

package io.shardingsphere.core.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.event.overall.OverallExecutionEvent;
import io.shardingsphere.core.executor.event.sql.SQLExecutionEvent;
import io.shardingsphere.core.executor.event.sql.SQLExecutionEventFactory;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.threadlocal.ExecutorExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SQL execute engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@Slf4j
public abstract class ExecutorEngine implements AutoCloseable {
    
    private static final ExecutorService SHUTDOWN_EXECUTOR = Executors.newSingleThreadExecutor(ShardingThreadFactoryBuilder.build("ExecutorEngineCloser"));
    
    @Getter
    private final ListeningExecutorService executorService;
    
    private final EventBus shardingEventBus;
    
    public ExecutorEngine(final int executorSize) {
        executorService = MoreExecutors.listeningDecorator(
                0 == executorSize ? Executors.newCachedThreadPool(ShardingThreadFactoryBuilder.build()) : Executors.newFixedThreadPool(executorSize, ShardingThreadFactoryBuilder.build()));
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
        shardingEventBus = ShardingEventBusInstance.getInstance();
    }
    
    /**
     * Execute.
     *
     * @param baseStatementUnits statement execute unitS
     * @param executeCallback prepared statement execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final Collection<? extends BaseStatementUnit> baseStatementUnits, final ExecuteCallback<T> executeCallback) throws SQLException {
        if (baseStatementUnits.isEmpty()) {
            return Collections.emptyList();
        }
        OverallExecutionEvent event = new OverallExecutionEvent(executeCallback.getSQLType(), baseStatementUnits.size() > 1);
        shardingEventBus.post(event);
        try {
            List<T> result = getExecuteResults(baseStatementUnits, executeCallback);
            event.setExecuteSuccess();
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            event.setExecuteFailure(ex);
            ExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        } finally {
            shardingEventBus.post(event);
        }
    }
    
    protected abstract <T> List<T> getExecuteResults(Collection<? extends BaseStatementUnit> baseStatementUnits, ExecuteCallback<T> executeCallback) throws Exception;
    
    protected final <T> T executeInternal(
            final BaseStatementUnit baseStatementUnit, final ExecuteCallback<T> executeCallback, final boolean isExceptionThrown, final Map<String, Object> dataMap) throws Exception {
        T result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        List<SQLExecutionEvent> events = new LinkedList<>();
        for (List<Object> each : baseStatementUnit.getSqlExecutionUnit().getSqlUnit().getParameterSets()) {
            SQLExecutionEvent event = SQLExecutionEventFactory.createEvent(executeCallback.getSQLType(), baseStatementUnit, each);
            events.add(event);
            shardingEventBus.post(event);
        }
        try {
            result = executeCallback.execute(baseStatementUnit);
            for (SQLExecutionEvent each : events) {
                each.setExecuteSuccess();
                shardingEventBus.post(each);
            }
            return result;
        } catch (final SQLException ex) {
            for (SQLExecutionEvent each : events) {
                each.setExecuteFailure(ex);
                shardingEventBus.post(each);
                ExecutorExceptionHandler.handleException(ex);
            }
            return null;
        }
    }
    
    @Override
    public final void close() {
        SHUTDOWN_EXECUTOR.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    executorService.shutdown();
                    while (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (final InterruptedException ex) {
                    log.error("ExecutorEngine can not been terminated", ex);
                }
            }
        });
    }
}
