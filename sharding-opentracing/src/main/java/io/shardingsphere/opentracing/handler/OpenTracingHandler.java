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

package io.shardingsphere.opentracing.handler;

import io.opentracing.Span;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.spi.ShardingEventHandler;
import io.shardingsphere.core.spi.ShardingFinishEvent;
import io.shardingsphere.core.spi.ShardingStartEvent;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.constant.ShardingErrorLogTags;
import io.shardingsphere.opentracing.constant.ShardingTags;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Opentracing listener.
 *
 * @author zhangliang
 * 
 * @param <S> type of sharding start event
 * @param <F> type of sharding finish event
 */
@RequiredArgsConstructor
public abstract class OpenTracingHandler<S extends ShardingStartEvent, F extends ShardingFinishEvent> implements ShardingEventHandler<S, F> {
    
    private final ThreadLocal<Span> spanHolder = new ThreadLocal<>();
    
    private final String operationName;
    
    @Override
    public final void handle(final S event) {
        spanHolder.set(initSpan(event, createSpanBuilder()));
    }
    
    @Override
    public final void handle(final F event) {
        if (null != event.getException()) {
            setErrorInfo(event);
        }
        tracingFinish(event);
    }
    
    private SpanBuilder createSpanBuilder() {
        return ShardingTracer.get().buildSpan(operationName).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    }
    
    protected abstract Span initSpan(S event, SpanBuilder spanBuilder);

    private void tracingFinish(final F event) {
        updateSpan(event, spanHolder.get());
        spanHolder.get().finish();
        spanHolder.remove();
        afterTracingFinish(event);
    }
    
    private void setErrorInfo(final F event) {
        spanHolder.get().setTag(Tags.ERROR.getKey(), true).log(System.currentTimeMillis(), getReason(event.getException()));
    }
    
    private Map<String, ?> getReason(final Throwable cause) {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put(ShardingErrorLogTags.EVENT, ShardingErrorLogTags.EVENT_ERROR_TYPE);
        result.put(ShardingErrorLogTags.ERROR_KIND, cause.getClass().getName());
        result.put(ShardingErrorLogTags.MESSAGE, cause.getMessage());
        return result;
    }
    
    protected void updateSpan(final F event, final Span span) {
    }
    
    protected void afterTracingFinish(final F event) {
    }
}
