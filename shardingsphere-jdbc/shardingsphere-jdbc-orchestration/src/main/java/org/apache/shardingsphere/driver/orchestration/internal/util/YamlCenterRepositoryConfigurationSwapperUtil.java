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
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.swapper.CenterRepositoryConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML instance configuration swapper util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlCenterRepositoryConfigurationSwapperUtil {
    
    private static final CenterRepositoryConfigurationYamlSwapper INSTANCE_SWAPPER = new CenterRepositoryConfigurationYamlSwapper();
    
    /**
     * Marshal YAML instance configuration map to instance configuration map.
     *
     * @param yamlInstanceConfigurationMap YAML instance configuration map
     * @return instance configuration map
     */
    public static Map<String, CenterConfiguration> marshal(final Map<String, YamlCenterRepositoryConfiguration> yamlInstanceConfigurationMap) {
        Map<String, CenterConfiguration> result = new LinkedHashMap<>(yamlInstanceConfigurationMap.size(), 1);
        for (Map.Entry<String, YamlCenterRepositoryConfiguration> each : yamlInstanceConfigurationMap.entrySet()) {
            result.put(each.getKey(), INSTANCE_SWAPPER.swapToObject(each.getValue()));
        }
        return result;
    }
}
