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

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ShardingSphere event bus.
 */
public final class ShardingSphereEventBus {
    
    private static final String EVENT_BUS_NAME = "ShardingSphere-EventBus";
    
    private final Map<Class<?>, Collection<Object>> targetSubscribers = new ConcurrentHashMap<>();
    
    private final Lock lock = new ReentrantLock();
    
    private final ListeningExecutorService executorService;
    
    private final EventBus eventBus;
    
    private ShardingSphereEventBus() {
        executorService = MoreExecutors.listeningDecorator(getExecutorService());
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
        eventBus = new AsyncEventBus(EVENT_BUS_NAME, getExecutorService());
        register(new DeadEventService());
    }
    
    private ExecutorService getExecutorService() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(EVENT_BUS_NAME + "-%d").build();
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }
    
    /**
     * Get instance of ShardingSphere event bus.
     *
     * @return instance of ShardingSphere event bus
     */
    public static ShardingSphereEventBus getInstance() {
        return ShardingSphereEventBusHolder.INSTANCE;
    }
    
    /**
     * Registers all subscriber methods on {@code object} to receive events.
     *
     * @param target whose subscriber methods should be registered.
     * @param <T> subscriber type.
     */
    public <T> void register(final T target) {
        try {
            lock.lock();
            Arrays.stream(target.getClass().getDeclaredMethods()).filter(method -> {
                Class<?>[] parameterTypes = method.getParameterTypes();
                return parameterTypes.length == 1 && null != method.getDeclaredAnnotation(Subscribe.class);
            }).forEach(method -> {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> eventType = parameterTypes[0];
                if (!targetSubscribers.containsKey(eventType)) {
                    targetSubscribers.put(eventType, new LinkedHashSet<>());
                }
                targetSubscribers.get(eventType).add(target);
            });
            eventBus.register(new CompletableEventService<T>(target));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Post the event, blocking waiting for the result, default timeout 60 seconds.
     *
     * @param event eventbus event
     * @return completable event results.
     */
    public CompletableEventResult post(final Object event) {
        return post(event, 60);
    }

    /**
     * Post the event, blocking waiting for the result.
     *
     * @param event eventbus event
     * @param timeout SECONDS
     * @return completable event results.
     */
    public CompletableEventResult post(final Object event, final long timeout) {
        try {
            lock.lock();
            CompletableEventResult result = new CompletableEventResult(new HashMap<>());
            Collection<Object> subscribers = targetSubscribers.get(event.getClass());
            if (null != subscribers && !subscribers.isEmpty()) {
                Map<Object, CompletableFuture> completableFutures = new HashMap<>(subscribers.size());
                for (Object each : subscribers) {
                    completableFutures.put(each, new CompletableFuture());
                }
                CompletableEvent completableEvent = new CompletableEvent(event, completableFutures);
                eventBus.post(completableEvent);
                for (Map.Entry<Object, CompletableFuture> entry : completableFutures.entrySet()) {
                    result.getResult().put(entry.getKey(), entry.getValue().get(timeout, TimeUnit.SECONDS));
                }
            } else if (!(event instanceof DeadEvent)) {
                return post(new DeadEvent(eventBus, event));
            }
            return result;
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new ShardingSphereException(e);
        } finally {
            lock.unlock();
        }
    }

    private static final class ShardingSphereEventBusHolder {
        
        private static final ShardingSphereEventBus INSTANCE = new ShardingSphereEventBus();
    }
}
