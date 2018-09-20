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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.connection.GetConnectionEvent;
import io.shardingsphere.core.event.connection.GetConnectionFinishEvent;
import io.shardingsphere.core.event.connection.GetConnectionStartEvent;
import io.shardingsphere.opentracing.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;

/**
 * Get connection event listener.
 *
 * @author zhangyonglun
 */
public final class GetConnectionEventListener extends OpenTracingListener<GetConnectionEvent> {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/getConnection/";
    
    /**
     * Listen get connection event.
     *
     * @param event Get connection event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final GetConnectionEvent event) {
        tracing(event);
    }
    
    @Override
    protected void beforeExecute(final GetConnectionEvent event) {
        getSpan().set(ShardingTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.DB_INSTANCE.getKey(), ((GetConnectionStartEvent) event).getDataSource()).startManual());
    }
    
    @Override
    protected void tracingFinish(final GetConnectionEvent event) {
        GetConnectionFinishEvent finishEvent = (GetConnectionFinishEvent) event;
        Span span = getSpan().get();
        if (null != finishEvent.getDataSourceMetaData()) {
            span = span.setTag(Tags.PEER_HOSTNAME.getKey(), finishEvent.getDataSourceMetaData().getHostName())
                    .setTag(Tags.PEER_PORT.getKey(), finishEvent.getDataSourceMetaData().getPort())
                    .setTag(ShardingTags.CONNECTION_COUNT.getKey(), ((GetConnectionFinishEvent) event).getConnectionCount());
        }
        span.finish();
        getSpan().remove();
    }
    
    @Override
    protected Span getFailureSpan() {
        return getSpan().get();
    }
}
