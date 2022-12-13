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

package org.apache.shardingsphere.agent.core.advisor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.advisor.ClassAdvisor;
import org.apache.shardingsphere.agent.spi.AdvisorDefinitionService;

import java.util.Collection;

/**
 * Advisor definition service engine.
 */
@RequiredArgsConstructor
public final class AdvisorDefinitionServiceEngine {
    
    private final AdvisorDefinitionService advisorDefinitionService;
    
    /**
     * Get advisors.
     *
     * @param targetClassName target class name
     * @return advisors
     */
    public ClassAdvisor getAdvisors(final String targetClassName) {
        return ClassAdvisorRegistryFactory.getRegistry(advisorDefinitionService.getType()).getAdvisor(targetClassName);
    }
    
    /**
     * Get all advisors.
     * 
     * @param isEnhancedForProxy is enhanced for proxy
     * @return all advisors
     */
    public Collection<ClassAdvisor> getAllAdvisors(final boolean isEnhancedForProxy) {
        return isEnhancedForProxy ? advisorDefinitionService.getProxyAdvisors() : advisorDefinitionService.getJDBCAdvisors();
    }
}
