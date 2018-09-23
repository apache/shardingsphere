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
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.parsing.ParsingEvent;
import io.shardingsphere.core.event.parsing.ParsingStartEvent;
import io.shardingsphere.opentracing.constant.ShardingTags;

/**
 * SQL parsing event listener.
 *
 * @author zhangyonglun
 */
public final class ParsingEventListener extends OpenTracingListener<ParsingEvent> {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/parseSQL/";
    
    public ParsingEventListener() {
        super(OPERATION_NAME);
    }
    
    /**
     * Listen parsing event.
     *
     * @param event SQL parsing event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final ParsingEvent event) {
        tracing(event);
    }
    
    @Override
    protected Span initSpan(final ParsingEvent event, final SpanBuilder span) {
        return span.withTag(Tags.DB_STATEMENT.getKey(), ((ParsingStartEvent) event).getSql()).startManual();
    }
}
