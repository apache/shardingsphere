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

import io.opentracing.BaseSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.ShardingEvent;
import io.shardingsphere.core.event.ShardingEventBusInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * Opentracing listener.
 * 
 * @author zhangliang
 * 
 * @param <T> type of sharding event
 */
public abstract class OpenTracingListener<T extends ShardingEvent> {
    
    /**
     * Register listener.
     */
    public final void register() {
        ShardingEventBusInstance.getInstance().register(this);
    }
    
    @SuppressWarnings("unchecked")
    protected final void tracing(final T event) {
        switch (event.getEventType()) {
            case BEFORE_EXECUTE:
                beforeExecute(event);
                break;
            case EXECUTE_SUCCESS:
                tracingFinish();
                break;
            case EXECUTE_FAILURE:
                getFailureSpan().setTag(Tags.ERROR.getKey(), true).log(System.currentTimeMillis(), getReason(event.getException()));
                tracingFinish();
                break;
            default:
                throw new UnsupportedOperationException(event.getEventType().name());
        }
    }
    
    protected abstract void beforeExecute(T event);
    
    protected abstract void tracingFinish();
    
    protected abstract BaseSpan<?> getFailureSpan();
    
    private Map<String, ?> getReason(final Throwable cause) {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put("event", "error");
        result.put("error.kind", cause.getClass().getName());
        result.put("message", cause.getMessage());
        return result;
    }
}
