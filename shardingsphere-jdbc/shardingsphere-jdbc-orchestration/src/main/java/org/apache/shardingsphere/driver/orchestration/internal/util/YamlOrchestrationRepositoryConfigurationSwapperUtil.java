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

package org.apache.shardingsphere.driver.orchestration.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.swapper.OrchestrationCenterConfigurationYamlSwapper;

/**
 * YAML orchestration configuration swapper utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationRepositoryConfigurationSwapperUtil {
    
    private static final OrchestrationCenterConfigurationYamlSwapper SWAPPER = new OrchestrationCenterConfigurationYamlSwapper();
    
    /**
     * Marshal YAML orchestration repository configuration map to instance configuration map.
     *
     * @param orchestration YAML orchestration configuration
     * @return orchestration repository configuration map
     */
    public static OrchestrationConfiguration marshal(final YamlOrchestrationConfiguration orchestration) {
        if (null == orchestration.getAdditionalConfigCenter()) {
            return new OrchestrationConfiguration(orchestration.getName(), SWAPPER.swapToObject(orchestration.getRegistryCenter()), orchestration.isOverwrite());
        }
        return new OrchestrationConfiguration(orchestration.getName(),
                SWAPPER.swapToObject(orchestration.getRegistryCenter()), SWAPPER.swapToObject(orchestration.getAdditionalConfigCenter()), orchestration.isOverwrite());
    }
}
