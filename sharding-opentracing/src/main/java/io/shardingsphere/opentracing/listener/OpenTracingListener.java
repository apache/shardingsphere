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

import io.opentracing.Span;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.ShardingEvent;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.opentracing.constant.ShardingErrorLogTags;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Opentracing listener.
 *
 * @author zhangliang
 * 
 * @param <T> type of sharding event
 */
@RequiredArgsConstructor
public abstract class OpenTracingListener<T extends ShardingEvent> {
    
    private final ThreadLocal<Span> spanHolder = new ThreadLocal<>();
    
    private final String operationName;
    
    /**
     * Register listener.
     */
    public final void register() {
        ShardingEventBusInstance.getInstance().register(this);
    }
    
    protected final void tracing(final T event) {
        switch (event.getEventType()) {
            case BEFORE_EXECUTE:
                spanHolder.set(initSpan(event, createSpanBuilder()));
                break;
            case EXECUTE_SUCCESS:
                tracingFinish(event);
                break;
            case EXECUTE_FAILURE:
                setErrorInfo(event);
                tracingFinish(event);
                break;
            default:
                throw new UnsupportedOperationException(event.getEventType().name());
        }
    }
    
    private SpanBuilder createSpanBuilder() {
        return ShardingTracer.get().buildSpan(operationName).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    }
    
    protected abstract Span initSpan(T event, SpanBuilder spanBuilder);
    
    private void tracingFinish(final T event) {
        updateSpan(event, spanHolder.get());
        spanHolder.get().finish();
        spanHolder.remove();
        afterTracingFinish(event);
    }
    
    protected void updateSpan(final T event, final Span span) {
    }
    
    protected void afterTracingFinish(final T event) {
    }
    
    private void setErrorInfo(final T event) {
        spanHolder.get().setTag(Tags.ERROR.getKey(), true).log(System.currentTimeMillis(), getReason(event.getException()));
    }
    
    private Map<String, ?> getReason(final Throwable cause) {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put(ShardingErrorLogTags.EVENT, ShardingErrorLogTags.EVENT_ERROR_TYPE);
        result.put(ShardingErrorLogTags.ERROR_KIND, cause.getClass().getName());
        result.put(ShardingErrorLogTags.MESSAGE, cause.getMessage());
        return result;
    }
}
