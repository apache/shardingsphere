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

package org.apache.shardingsphere.agent.core.advisor.executor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.AgentAdvice;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advice factory.
 */
@RequiredArgsConstructor
public final class AdviceFactory {
    
    private static final Map<String, AgentAdvice> CACHED_ADVICES = new ConcurrentHashMap<>();
    
    private final ClassLoaderContext classLoaderContext;
    
    /**
     * Get advice.
     *
     * @param adviceClassName advice class name
     * @return got advance
     */
    public AgentAdvice getAdvice(final String adviceClassName) {
        ClassLoader appClassLoader = classLoaderContext.getAppClassLoader();
        String cacheKey = String.format("%s_%s@%s", adviceClassName, appClassLoader.getClass().getName(), Integer.toHexString(appClassLoader.hashCode()));
        return CACHED_ADVICES.computeIfAbsent(cacheKey, key -> createAdvice(adviceClassName));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private AgentAdvice createAdvice(final String adviceClassName) {
        return (AgentAdvice) Class.forName(adviceClassName, true, classLoaderContext.getPluginClassLoader()).getDeclaredConstructor().newInstance();
    }
}
