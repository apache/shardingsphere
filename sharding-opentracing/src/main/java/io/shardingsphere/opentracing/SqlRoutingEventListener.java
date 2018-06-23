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

package io.shardingsphere.opentracing;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.routing.event.SqlRoutingEvent;
import io.shardingsphere.opentracing.sampling.SamplingService;
import io.shardingsphere.opentracing.tag.LocalTags;

import java.util.HashMap;
import java.util.Map;

/**
 * Listen to the event of sql routing execution.
 *
 * @author chenqingyang
 */
public final class SqlRoutingEventListener {
    
    private static final String OPER_NAME_PREFIX = "/SHARDING-SPHERE/ROUTING/";
    
    private final ThreadLocal<ActiveSpan> spanContainer = new ThreadLocal<>();
    
    /**
     * listen sql routing event.
     *
     * @param event sql routing event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listenSqlRoutingEvent(final SqlRoutingEvent event) {
        if (!SamplingService.getInstance().trySampling()) {
            return;
        }
        Tracer tracer = ShardingJDBCTracer.get();
        ActiveSpan activeSpan;
        switch (event.getEventRoutingType()) {
            case BEFORE_ROUTE:
                activeSpan = tracer.buildSpan(OPER_NAME_PREFIX)
                        .withTag(Tags.COMPONENT.getKey(), LocalTags.COMPONENT_NAME)
                        .withTag(Tags.DB_STATEMENT.getKey(), event.getSql())
                        .startActive();
                spanContainer.set(activeSpan);
                break;
            case ROUTE_FAILURE:
                activeSpan = spanContainer.get();
                activeSpan.setTag(Tags.ERROR.getKey(), true);
                if (event.getException().isPresent()) {
                    activeSpan.log(System.currentTimeMillis(), log(event.getException().get()));
                }
                deactivate();
                break;
            case ROUTE_SUCCESS:
                deactivate();
                break;
            default:
                throw new ShardingException("Unsupported event type");
        }
    }
    
    private void deactivate() {
        spanContainer.get().deactivate();
        spanContainer.remove();
    }
    
    private Map<String, ?> log(final Throwable t) {
        Map<String, String> result = new HashMap<>(3);
        result.put("event", "error");
        result.put("error.kind", t.getClass().getName());
        result.put("message", t.getMessage());
        return result;
    }
}
