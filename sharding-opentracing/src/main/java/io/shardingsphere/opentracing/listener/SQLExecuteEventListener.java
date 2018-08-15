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

package io.shardingsphere.opentracing.listener;

import com.google.common.base.Joiner;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.executor.event.SQLExecutionEvent;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.tag.LocalTags;

/**
 * SQL execute event listener.
 * 
 * @author gaohongtao
 * @author wangkai
 * @author maxiaoguang
 */
public abstract class SQLExecuteEventListener extends TracingListener<SQLExecutionEvent> {
    
    private static final String SNAPSHOT_DATA_KEY = "OPENTRACING_SNAPSHOT_DATA";

    private static final String OPERATION_NAME_PREFIX = "/SHARDING-SPHERE/EXECUTE/";

    private final ThreadLocal<ActiveSpan> trunkContainer = new ThreadLocal<>();
    
    private final ThreadLocal<Span> branchContainer = new ThreadLocal<>();
    
    private final ThreadLocal<ActiveSpan> trunkInBranchContainer = new ThreadLocal<>();
    
    @Override
    protected final void beforeExecute(final SQLExecutionEvent event) {
        Tracer tracer = ShardingTracer.get();
        if (ExecutorDataMap.getDataMap().containsKey(SNAPSHOT_DATA_KEY) && null == trunkContainer.get() && null == branchContainer.get()) {
            trunkInBranchContainer.set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(SNAPSHOT_DATA_KEY)).activate());
        }
        if (null == branchContainer.get()) {
            branchContainer.set(tracer.buildSpan(OPERATION_NAME_PREFIX + getOperation()).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                    .withTag(Tags.PEER_HOSTNAME.getKey(), event.getDataSource()).withTag(Tags.COMPONENT.getKey(), LocalTags.COMPONENT_NAME)
                    .withTag(Tags.DB_INSTANCE.getKey(), event.getDataSource()).withTag(Tags.DB_TYPE.getKey(), "sql")
                    .withTag(LocalTags.DB_BIND_VARIABLES.getKey(), event.getParameters().isEmpty() ? "" : Joiner.on(",").join(event.getParameters()))
                    .withTag(Tags.DB_STATEMENT.getKey(), event.getSqlUnit().getSql()).startManual());
        }
    }
    
    @Override
    protected final void tracingFinish() {
        if (null == branchContainer.get()) {
            return;
        }
        branchContainer.get().finish();
        branchContainer.remove();
        if (null == trunkInBranchContainer.get()) {
            return;
        }
        trunkInBranchContainer.get().deactivate();
        trunkInBranchContainer.remove();
    }
    
    @Override
    protected final void tracingFailure(final SQLExecutionEvent event) {
        Span span = branchContainer.get();
        span.setTag(Tags.ERROR.getKey(), true);
        if (event.getException().isPresent()) {
            span.log(System.currentTimeMillis(), log(event.getException().get()));
        }
    }
    
    protected abstract String getOperation();
}
