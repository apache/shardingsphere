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

package io.shardingsphere.opentracing.listener.merger;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.ActiveSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.merger.MergeEvent;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.listener.OpenTracingListener;
import io.shardingsphere.opentracing.ShardingTags;

/**
 * Result set merge event listener.
 *
 * @author chenqingyang
 */
public final class MergeEventListener extends OpenTracingListener<MergeEvent> {
    
    private static final String OPERATION_NAME_PREFIX = "/Sharding-Sphere/merge/";
    
    private final ThreadLocal<ActiveSpan> span = new ThreadLocal<>();
    
    /**
     * Listen result set merge event.
     *
     * @param event merge event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final MergeEvent event) {
        tracing(event);
    }
    
    @Override
    protected void beforeExecute(final MergeEvent event) {
        span.set(ShardingTracer.get().buildSpan(OPERATION_NAME_PREFIX).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).startActive());
    }
    
    @Override
    protected void tracingFinish() {
        span.get().deactivate();
        span.remove();
    }
    
    @Override
    protected ActiveSpan getFailureSpan() {
        return span.get();
    }
}
