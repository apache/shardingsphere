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

import com.google.common.eventbus.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Completable event service.
 */
public final class CompletableEventService<T> {

    private final T target;

    private final Map<Class<?>, Method> targetMethods;

    public CompletableEventService(final T target) {
        this.target = target;
        this.targetMethods = Arrays.stream(target.getClass().getDeclaredMethods()).filter(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            return parameterTypes.length == 1 && null != method.getDeclaredAnnotation(Subscribe.class);
        }).collect(Collectors.toMap(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            return parameterTypes[0];
        }, method -> method));
    }

    /**
     * Handle event.
     *
     * @param completableEvent completable event
     */
    @Subscribe
    public void handle(final CompletableEvent completableEvent) {
        try {
            Method handler = targetMethods.get(completableEvent.getTarget().getClass());
            if (null == handler) {
                return;
            }
            Object result;
            if (handler.getReturnType().equals(Void.TYPE)) {
                handler.invoke(target, completableEvent.getTarget());
                result = null;
            } else {
                result = handler.invoke(target, completableEvent.getTarget());
            }
            ((CompletableFuture) completableEvent.getCompletableFutures().get(target)).complete(result);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            ((CompletableFuture) completableEvent.getCompletableFutures().get(target)).completeExceptionally(e);
        }
    }
}
