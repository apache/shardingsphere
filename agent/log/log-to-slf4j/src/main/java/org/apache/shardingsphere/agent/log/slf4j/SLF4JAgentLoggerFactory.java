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

package org.apache.shardingsphere.agent.log.slf4j;

import org.apache.shardingsphere.agent.log.api.AgentLogger;
import org.apache.shardingsphere.agent.log.spi.AgentLoggerFactorySPI;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SLF4J agent logger factory.
 */
public final class SLF4JAgentLoggerFactory implements AgentLoggerFactorySPI {
    
    private static final Map<String, AgentLogger> LOGGER_MAP = new ConcurrentHashMap<>();
    
    @Override
    public AgentLogger getAgentLogger(final Class<?> clazz) {
        return getAgentLogger(clazz.getName());
    }
    
    @Override
    public AgentLogger getAgentLogger(final String name) {
        return LOGGER_MAP.computeIfAbsent(name, key -> new SLF4JAgentLogger(LoggerFactory.getLogger(name)));
    }
}
