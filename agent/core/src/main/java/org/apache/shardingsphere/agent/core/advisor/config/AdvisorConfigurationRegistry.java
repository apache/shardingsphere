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

package org.apache.shardingsphere.agent.core.advisor.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advisor configuration registry.
 */
public final class AdvisorConfigurationRegistry {
    
    private final Map<String, AdvisorConfiguration> advisors = new ConcurrentHashMap<>();
    
    /**
     * Get advisor configuration.
     * 
     * @param targetClassName to be advised class name
     * @return advisor configuration
     */
    public AdvisorConfiguration getAdvisorConfiguration(final String targetClassName) {
        return advisors.computeIfAbsent(targetClassName, AdvisorConfiguration::new);
    }
}
