/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.eventbus;

import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ShardingSphere event bus.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereEventBus {
    
    /**
     * Get instance of ShardingSphere event bus.
     *
     * @return instance of ShardingSphere event bus
     */
    public static EventBus getInstance() {
        return ShardingSphereEventBusHolder.INSTANCE;
    }
    
    /**
     * Registers all subscriber methods on {@code object} to receive events.
     *
     * @param target whose subscriber methods should be registered.
     * @param <T> subscriber type.
     */
    public static <T> void register(final T target) {
        getInstance().register(new CompletableEventService<T>(target) {
        });
    }

    /**
     * Post the event, blocking waiting for the result, default timeout 60 seconds.
     *
     * @param event eventbus event
     * @param <T> The value type returned after the event is consumed
     * @return T The value returned after the event is consumed
     */
    public static <T> T postEvent(final CompletableEvent event) {
        return postEvent(event, 60);
    }

    /**
     * Post the event, blocking waiting for the result.
     *
     * @param event eventbus event
     * @param timeout SECONDS
     * @param <T> The value type returned after the event is consumed
     * @return T The value returned after the event is consumed
     */
    public static <T> T postEvent(final CompletableEvent event, final long timeout) {
        try {
            getInstance().post(event);
            return (T) event.getFuture().get(timeout, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new ShardingSphereException(e);
        }
    }

    private static final class ShardingSphereEventBusHolder {
        
        private static final EventBus INSTANCE = new EventBus("ShardingSphere-EventBus");

        private static final DummyEventService DUMMY_EVENT_SERVICE = new DummyEventService();
    }
}
