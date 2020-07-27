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

package org.apache.shardingsphere.orchestration.repository.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Orchestration configuration.
 */
@RequiredArgsConstructor
@Getter
public final class OrchestrationConfiguration {
    
    private final String name;
    
    private final OrchestrationCenterConfiguration registryCenterConfiguration;
    
    private final OrchestrationCenterConfiguration additionalConfigCenterConfiguration;
    
    private final boolean overwrite;
    
    public OrchestrationConfiguration(final String name, final OrchestrationCenterConfiguration orchestrationCenterConfig, final boolean overwrite) {
        this(name, orchestrationCenterConfig, null, overwrite);
    }
    
    /**
     * Get additional config center configuration.
     * 
     * @return additional config center configuration
     */
    public Optional<OrchestrationCenterConfiguration> getAdditionalConfigCenterConfiguration() {
        return Optional.ofNullable(additionalConfigCenterConfiguration);
    }
}
