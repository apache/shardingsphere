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
import io.shardingsphere.core.event.parsing.ParsingEvent;
import io.shardingsphere.opentracing.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;

/**
 * SQL parsing event listener.
 *
 * @author zhangyonglun
 */
public final class ParsingEventListener extends OpenTracingListener<ParsingEvent> {
    
    private static final String OPERATION_NAME_PREFIX = "/Sharding-Sphere/parsing/";
    
    private final ThreadLocal<Span> branchSpan = new ThreadLocal<>();
    
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
    protected void beforeExecute(final ParsingEvent event) {
        branchSpan.set(ShardingTracer.get().buildSpan(OPERATION_NAME_PREFIX)
            .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).withTag(Tags.DB_STATEMENT.getKey(), event.getSql()).startManual());
    }
    
    @Override
    protected void tracingFinish() {
        if (null == branchSpan.get()) {
            return;
        }
        branchSpan.get().finish();
        branchSpan.remove();
    }
    
    @Override
    protected Span getFailureSpan() {
        return branchSpan.get();
    }
}
