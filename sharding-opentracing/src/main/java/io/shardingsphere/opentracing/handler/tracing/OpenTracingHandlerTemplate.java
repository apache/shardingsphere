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

package io.shardingsphere.opentracing.handler.tracing;

import io.opentracing.Span;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.constant.ShardingErrorLogTags;
import io.shardingsphere.opentracing.constant.ShardingTags;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Opentracing handler template.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OpenTracingHandlerTemplate {
    
    private final ThreadLocal<Span> spanHolder = new ThreadLocal<>();
    
    private final String operationName;
    
    /**
     * Start record span.
     * 
     * @param spanStartCallback span start callback
     */
    public void start(final OpenTracingSpanStartCallback spanStartCallback) {
        spanHolder.set(spanStartCallback.initSpan(createSpanBuilder()));
    }
    
    /**
     * Finish record succeed span.
     * 
     * @param spanFinishCallback span finish callback
     */
    public void finishSuccess(final OpenTracingSpanFinishCallback spanFinishCallback) {
        tracingFinish(spanFinishCallback);
    }
    
    private SpanBuilder createSpanBuilder() {
        return ShardingTracer.get().buildSpan(operationName).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    }
    
    private void tracingFinish(final OpenTracingSpanFinishCallback spanFinishCallback) {
        spanHolder.get().finish();
        spanHolder.remove();
        spanFinishCallback.afterTracingFinish();
    }
    
    /**
     * Finish record failed span.
     *
     * @param cause failure cause
     * @param spanFinishCallback span finish callback
     */
    public void finishFailure(final Exception cause, final OpenTracingSpanFinishCallback spanFinishCallback) {
        setErrorInfo(cause);
        tracingFinish(spanFinishCallback);
    }
    
    private void setErrorInfo(final Exception cause) {
        spanHolder.get().setTag(Tags.ERROR.getKey(), true).log(System.currentTimeMillis(), getReason(cause));
    }
    
    private Map<String, ?> getReason(final Throwable cause) {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put(ShardingErrorLogTags.EVENT, ShardingErrorLogTags.EVENT_ERROR_TYPE);
        result.put(ShardingErrorLogTags.ERROR_KIND, cause.getClass().getName());
        result.put(ShardingErrorLogTags.MESSAGE, cause.getMessage());
        return result;
    }
}
