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

package io.shardingsphere.opentracing;

import com.google.common.base.Joiner;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.executor.event.AbstractSQLExecutionEvent;
import io.shardingsphere.core.executor.event.DMLExecutionEvent;
import io.shardingsphere.core.executor.event.DQLExecutionEvent;
import io.shardingsphere.core.executor.event.OverallExecutionEvent;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.sampling.SamplingService;
import io.shardingsphere.opentracing.tag.LocalTags;
import java.util.HashMap;
import java.util.Map;

/**
 * Listen to the event of sql execution.
 * 
 * @author gaohongtao
 * @author wangkai
 * @author maxiaoguang
 */
public final class ExecuteEventListener {
    
    private static final String SNAPSHOT_DATA_KEY = "OPENTRACING_SNAPSHOT_DATA";

    private static final String OPER_NAME_PREFIX = "/SHARDING-SPHERE/EXECUTE/";

    private final ThreadLocal<ActiveSpan> trunkContainer = new ThreadLocal<>();
    
    private final ThreadLocal<Span> branchContainer = new ThreadLocal<>();
    
    private final ThreadLocal<ActiveSpan> trunkInBranchContainer = new ThreadLocal<>();
    
    /**
     * listen overall sql execution event.
     *
     * @param event Overall sql execution event.
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listenOverall(final OverallExecutionEvent event) {
        if (!SamplingService.getInstance().trySampling()) {
            return;
        }
        Tracer tracer = ShardingJDBCTracer.get();
        ActiveSpan activeSpan;
        switch (event.getEventExecutionType()) {
            case BEFORE_EXECUTE:
                activeSpan = tracer.buildSpan(OPER_NAME_PREFIX + event.getSqlType().name()).withTag(Tags.COMPONENT.getKey(), LocalTags.COMPONENT_NAME)
                        .startActive();
                trunkContainer.set(activeSpan);
                if (isParallelExecute(event)) {
                    ExecutorDataMap.getDataMap().put(SNAPSHOT_DATA_KEY, activeSpan.capture());
                }
                break;
            case EXECUTE_FAILURE:
                activeSpan = trunkContainer.get();
                activeSpan.setTag(Tags.ERROR.getKey(), true);
                if (event.getException().isPresent()) {
                    activeSpan.log(System.currentTimeMillis(), log(event.getException().get()));
                }
                deactivate();
                break;
            case EXECUTE_SUCCESS:
                deactivate();
                break;
            default:
                throw new ShardingException("Unsupported event type");
        }
    }
    
    private boolean isParallelExecute(final OverallExecutionEvent event) {
        return event.getStatementUnitSize() > 1;
    }
    
    private void deactivate() {
        trunkContainer.get().deactivate();
        trunkContainer.remove();
        SamplingService.getInstance().samplingAdd();
    }
    
    /**
     * listen DML execution event.
     *
     * @param event DML execution event.
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listenDML(final DMLExecutionEvent event) {
        handle(event, "MODIFY");
    }
    
    /**
     * listen DQL execution event.
     *
     * @param event DQL execution event.
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listenDQL(final DQLExecutionEvent event) {
        handle(event, "QUERY");
    }
    
    private void handle(final AbstractSQLExecutionEvent event, final String operation) {
        if (!SamplingService.getInstance().trySampling()) {
            return;
        }
        Tracer tracer = ShardingJDBCTracer.get();
        switch (event.getEventExecutionType()) {
            case BEFORE_EXECUTE:
                if (ExecutorDataMap.getDataMap().containsKey(SNAPSHOT_DATA_KEY) && !isCurrentMainThread() && branchContainer.get() == null) {
                    trunkInBranchContainer.set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(SNAPSHOT_DATA_KEY)).activate());
                }

                String params = "";
                if (!event.getParameters().isEmpty()) {
                    params = Joiner.on(",").join(event.getParameters());
                }
                if (branchContainer.get() == null) {
                    branchContainer.set(tracer.buildSpan(OPER_NAME_PREFIX + operation).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                            .withTag(Tags.PEER_HOSTNAME.getKey(), event.getDataSource()).withTag(Tags.COMPONENT.getKey(), LocalTags.COMPONENT_NAME)
                            .withTag(Tags.DB_INSTANCE.getKey(), event.getDataSource()).withTag(Tags.DB_TYPE.getKey(), "sql")
                            .withTag(LocalTags.DB_BIND_VARIABLES.getKey(), params)
                            .withTag(Tags.DB_STATEMENT.getKey(), event.getSqlUnit().getSql()).startManual());
                }
                break;
            case EXECUTE_FAILURE:
                Span span = branchContainer.get();
                span.setTag(Tags.ERROR.getKey(), true);
                if (event.getException().isPresent()) {
                    span.log(System.currentTimeMillis(), log(event.getException().get()));
                }
                finish();
                break;
            case EXECUTE_SUCCESS:
                finish();
                break;
            default:
                throw new ShardingException("Unsupported event type");
        }
    }
    
    private boolean isCurrentMainThread() {
        return null != trunkContainer.get();
    }
    
    private void finish() {
        if (branchContainer.get() != null) {
            branchContainer.get().finish();
            branchContainer.remove();
            if (null == trunkInBranchContainer.get()) {
                return;
            }
            trunkInBranchContainer.get().deactivate();
            trunkInBranchContainer.remove();
        }
    }
    
    private Map<String, ?> log(final Throwable t) {
        Map<String, String> result = new HashMap<>(3);
        result.put("event", "error");
        result.put("error.kind", t.getClass().getName());
        result.put("message", t.getMessage());
        return result;
    }
}
