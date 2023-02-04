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

package org.apache.shardingsphere.agent.core.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.log.slf4j.SLF4JAgentLogger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent logger factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentLoggerFactory {
    
    private static final Map<String, AgentLogger> LOGGERS = new ConcurrentHashMap<>();
    
    /**
     * Get agent logger.
     *
     * @param clazz clazz
     * @return agent logger
     */
    public static AgentLogger getAgentLogger(final Class<?> clazz) {
        return AgentLoggerFactory.getAgentLogger(clazz.getName());
    }
    
    /**
     * Get agent logger.
     *
     * @param name name
     * @return agent logger
     */
    public static AgentLogger getAgentLogger(final String name) {
        return LOGGERS.computeIfAbsent(name, key -> new SLF4JAgentLogger(LoggerFactory.getLogger(name)));
    }
}
