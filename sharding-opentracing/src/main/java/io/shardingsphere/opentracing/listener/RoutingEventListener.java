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
import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.routing.event.RoutingEvent;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.tag.LocalTags;

/**
 * SQL route event listener.
 *
 * @author chenqingyang
 */
public final class RoutingEventListener extends TracingListener<RoutingEvent> {
    
    private static final String OPERATION_NAME_PREFIX = "/SHARDING-SPHERE/ROUTING/";
    
    private final ThreadLocal<ActiveSpan> spanContainer = new ThreadLocal<>();
    
    /**
     * Listen routing event.
     *
     * @param event SQL routing event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final RoutingEvent event) {
        tracing(event);
    }
    
    @Override
    protected void beforeExecute(final RoutingEvent event) {
        Tracer tracer = ShardingTracer.get();
        ActiveSpan activeSpan = tracer.buildSpan(OPERATION_NAME_PREFIX)
                .withTag(Tags.COMPONENT.getKey(), LocalTags.COMPONENT_NAME)
                .withTag(Tags.DB_STATEMENT.getKey(), event.getSql())
                .startActive();
        spanContainer.set(activeSpan);
    }
    
    @Override
    protected void tracingFinish() {
        spanContainer.get().deactivate();
        spanContainer.remove();
    }
    
    @Override
    protected void tracingFailure(final RoutingEvent event) {
        ActiveSpan activeSpan = spanContainer.get();
        activeSpan.setTag(Tags.ERROR.getKey(), true);
        if (event.getException().isPresent()) {
            activeSpan.log(System.currentTimeMillis(), log(event.getException().get()));
        }
    }
}
