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

package io.shardingsphere.opentracing.handler.connection;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.spi.event.connection.get.GetConnectionEventHandler;
import io.shardingsphere.core.spi.event.connection.get.GetConnectionFinishEvent;
import io.shardingsphere.core.spi.event.connection.get.GetConnectionStartEvent;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.handler.OpenTracingHandler;
import io.shardingsphere.opentracing.handler.root.OpenTracingRootInvokeHandler;

/**
 * Open tracing get connection event handler.
 *
 * @author zhangliang
 */
public final class OpenTracingGetConnectionEventHandler extends OpenTracingHandler<GetConnectionStartEvent, GetConnectionFinishEvent> implements GetConnectionEventHandler {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/getConnection/";
    
    private final ThreadLocal<Boolean> isTrunkThread = new ThreadLocal<>();
    
    public OpenTracingGetConnectionEventHandler() {
        super(OPERATION_NAME);
    }
    
    @Override
    protected Span initSpan(final GetConnectionStartEvent event, final SpanBuilder spanBuilder) {
        isTrunkThread.set(OpenTracingRootInvokeHandler.isTrunkThread());
        if (ExecutorDataMap.getDataMap().containsKey(OpenTracingRootInvokeHandler.ROOT_SPAN_CONTINUATION) && !isTrunkThread.get()) {
            OpenTracingRootInvokeHandler.getActiveSpan().set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(OpenTracingRootInvokeHandler.ROOT_SPAN_CONTINUATION)).activate());
        }
        return spanBuilder.withTag(Tags.DB_INSTANCE.getKey(), event.getDataSource()).startManual();
    }
    
    @Override
    protected void updateSpan(final GetConnectionFinishEvent event, final Span span) {
        if (null != event.getDataSourceMetaData()) {
            span.setTag(Tags.PEER_HOSTNAME.getKey(), event.getDataSourceMetaData().getHostName())
                    .setTag(Tags.PEER_PORT.getKey(), event.getDataSourceMetaData().getPort())
                    .setTag(ShardingTags.CONNECTION_COUNT.getKey(), event.getConnectionCount());
        }
    }
    
    @Override
    protected void afterTracingFinish(final GetConnectionFinishEvent event) {
        if (!isTrunkThread.get()) {
            OpenTracingRootInvokeHandler.getActiveSpan().get().deactivate();
            OpenTracingRootInvokeHandler.getActiveSpan().remove();
        }
    }
}
