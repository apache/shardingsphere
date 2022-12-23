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

package org.apache.shardingsphere.agent.core.transformer.builder.advise;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.advice.AgentAdvice;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy Advice factory.
 */
@RequiredArgsConstructor
public final class ProxyAdviceFactory {
    
    private static final Map<String, AgentAdvice> CACHED_ADVICES = new ConcurrentHashMap<>();
    
    /**
     * Get advice.
     *
     * @param adviceClassName advice class name
     * @return got advance
     */
    public AgentAdvice getAdvice(final String adviceClassName) {
        return CACHED_ADVICES.computeIfAbsent(adviceClassName, this::createAdviceForProxy);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private AgentAdvice createAdviceForProxy(final String adviceClassName) {
        return (AgentAdvice) Class.forName(adviceClassName, true, AgentClassLoader.getClassLoader()).getDeclaredConstructor().newInstance();
    }
}
