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
import org.apache.shardingsphere.orchestration.repository.api.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.swapper.OrchestrationRepositoryConfigurationYamlSwapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML orchestration configuration swapper utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationRepositoryConfigurationSwapperUtil {
    
    private static final OrchestrationRepositoryConfigurationYamlSwapper SWAPPER = new OrchestrationRepositoryConfigurationYamlSwapper();
    
    /**
     * Marshal YAML orchestration repository configuration map to instance configuration map.
     *
     * @param yamlConfigurationMap YAML orchestration repository configuration map
     * @return orchestration repository configuration map
     */
    public static Map<String, CenterConfiguration> marshal(final Map<String, YamlOrchestrationRepositoryConfiguration> yamlConfigurationMap) {
        Map<String, CenterConfiguration> result = new LinkedHashMap<>(yamlConfigurationMap.size(), 1);
        for (Entry<String, YamlOrchestrationRepositoryConfiguration> each : yamlConfigurationMap.entrySet()) {
            result.put(each.getKey(), SWAPPER.swapToObject(each.getValue()));
        }
        return result;
    }
}
