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

package io.shardingsphere.opentracing.handler.tracing.parsing;

import io.opentracing.Span;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.spi.parsing.ParsingHook;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.handler.tracing.OpenTracingHandlerTemplate;
import io.shardingsphere.opentracing.handler.tracing.OpenTracingSpanFinishCallbackAdapter;
import io.shardingsphere.opentracing.handler.tracing.OpenTracingSpanStartCallback;

/**
 * Open tracing parsing hook.
 *
 * @author zhangliang
 */
public final class OpenTracingParsingHook implements ParsingHook {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/parseSQL/";
    
    private final OpenTracingHandlerTemplate handlerTemplate = new OpenTracingHandlerTemplate(OPERATION_NAME);
    
    @Override
    public void start(final String sql) {
        handlerTemplate.start(new OpenTracingSpanStartCallback() {
            
            @Override
            public Span initSpan(final SpanBuilder spanBuilder) {
                return spanBuilder.withTag(Tags.DB_STATEMENT.getKey(), sql).startManual();
            }
        });
    }
    
    @Override
    public void finishSuccess() {
        handlerTemplate.finishSuccess(new OpenTracingSpanFinishCallbackAdapter());
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        handlerTemplate.finishFailure(cause, new OpenTracingSpanFinishCallbackAdapter());
    }
}
