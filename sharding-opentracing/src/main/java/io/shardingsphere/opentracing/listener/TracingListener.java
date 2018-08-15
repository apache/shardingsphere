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

import io.shardingsphere.core.event.ShardingEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.opentracing.sampling.SamplingService;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracing listener.
 * 
 * @author zhangliang
 * 
 * @param <T> type of sharding event
 */
@Getter
public abstract class TracingListener<T extends ShardingEvent> {
    
    private final SamplingService samplingService = SamplingService.getInstance();
    
    protected final void tracing(final T event) {
        if (!samplingService.trySampling()) {
            return;
        }
        switch (event.getEventType()) {
            case BEFORE_EXECUTE:
                beforeExecute(event);
                break;
            case EXECUTE_SUCCESS:
                tracingFinish();
                break;
            case EXECUTE_FAILURE:
                tracingFailure(event);
                tracingFinish();
                break;
            default:
                throw new ShardingException("Unsupported event type");
        }
    }
    
    protected abstract void beforeExecute(T event);
    
    protected abstract void tracingFinish();
    
    protected abstract void tracingFailure(T event);
    
    protected final Map<String, ?> log(final Throwable t) {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put("event", "error");
        result.put("error.kind", t.getClass().getName());
        result.put("message", t.getMessage());
        return result;
    }
}
