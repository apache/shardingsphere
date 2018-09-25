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

package io.shardingsphere.opentracing.handler.tracing.connection;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.spi.connection.get.GetConnectionHook;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.handler.root.OpenTracingRootInvokeHandler;
import io.shardingsphere.opentracing.handler.tracing.OpenTracingHandlerTemplate;
import io.shardingsphere.opentracing.handler.tracing.OpenTracingSpanFinishRootCleanCallbackAdapter;
import io.shardingsphere.opentracing.handler.tracing.OpenTracingSpanStartCallback;

/**
 * Open tracing get connection hook.
 *
 * @author zhangliang
 */
public final class OpenTracingGetConnectionHook implements GetConnectionHook {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/getConnection/";
    
    private final ThreadLocal<Boolean> isTrunkThread = new ThreadLocal<>();
    
    private final OpenTracingHandlerTemplate handlerTemplate = new OpenTracingHandlerTemplate(OPERATION_NAME);
    
    @Override
    public void start(final String dataSourceName) {
        handlerTemplate.start(new OpenTracingSpanStartCallback() {
            
            @Override
            public Span initSpan(final SpanBuilder spanBuilder) {
                isTrunkThread.set(OpenTracingRootInvokeHandler.isTrunkThread());
                if (ExecutorDataMap.getDataMap().containsKey(OpenTracingRootInvokeHandler.ROOT_SPAN_CONTINUATION) && !isTrunkThread.get()) {
                    OpenTracingRootInvokeHandler.getActiveSpan().set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(OpenTracingRootInvokeHandler.ROOT_SPAN_CONTINUATION)).activate());
                }
                return spanBuilder.withTag(Tags.DB_INSTANCE.getKey(), dataSourceName).startManual();
            }
        });
    }
    
    @Override
    public void finishSuccess(final DataSourceMetaData dataSourceMetaData, final int connectionCount) {
        handlerTemplate.finishSuccess(new OpenTracingSpanFinishRootCleanCallbackAdapter(isTrunkThread.get()) {
            
            @Override
            public void updateSpan(final Span span) {
                span.setTag(Tags.PEER_HOSTNAME.getKey(), dataSourceMetaData.getHostName())
                        .setTag(Tags.PEER_PORT.getKey(), dataSourceMetaData.getPort())
                        .setTag(ShardingTags.CONNECTION_COUNT.getKey(), connectionCount);
            }
        });
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        handlerTemplate.finishFailure(cause, new OpenTracingSpanFinishRootCleanCallbackAdapter(isTrunkThread.get()));
    }
}
