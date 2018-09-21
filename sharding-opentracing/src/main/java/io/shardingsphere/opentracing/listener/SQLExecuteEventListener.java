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
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.executor.SQLExecutionEvent;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;

/**
 * SQL execute event listener.
 *
 * @author gaohongtao
 * @author wangkai
 * @author maxiaoguang
 */
public final class SQLExecuteEventListener extends OpenTracingListener<SQLExecutionEvent> {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/executeSQL/";
    
    private final ThreadLocal<Boolean> isTrunkThread = new ThreadLocal<>();
    
    /**
     * Listen SQL execution event.
     *
     * @param event SQL execution event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final SQLExecutionEvent event) {
        tracing(event);
    }
    
    @Override
    protected Span beforeExecute(final SQLExecutionEvent event) {
        isTrunkThread.set(RootInvokeEventListener.isTrunkThread());
        if (ExecutorDataMap.getDataMap().containsKey(RootInvokeEventListener.OVERALL_SPAN_CONTINUATION) && !isTrunkThread.get()) {
            RootInvokeEventListener.getActiveSpan().set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(RootInvokeEventListener.OVERALL_SPAN_CONTINUATION)).activate());
        }
        return ShardingTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.PEER_HOSTNAME.getKey(), event.getDataSourceMetaData().getHostName())
                .withTag(Tags.PEER_PORT.getKey(), event.getDataSourceMetaData().getPort())
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .withTag(Tags.DB_INSTANCE.getKey(), event.getRouteUnit().getDataSourceName())
                .withTag(Tags.DB_TYPE.getKey(), "sql")
                .withTag(ShardingTags.DB_BIND_VARIABLES.getKey(), event.getParameters().isEmpty() ? "" : Joiner.on(",").join(event.getParameters()))
                .withTag(Tags.DB_STATEMENT.getKey(), event.getRouteUnit().getSqlUnit().getSql()).startManual();
    }
    
    @Override
    protected void afterTracingFinish(final SQLExecutionEvent event) {
        if (!isTrunkThread.get()) {
            RootInvokeEventListener.getActiveSpan().get().deactivate();
            RootInvokeEventListener.getActiveSpan().remove();
        }
    }
}
