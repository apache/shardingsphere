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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.util;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlInstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.swapper.InstanceConfigurationYamlSwapper;

/**
 * YamlInstanceConfiguration swapper util.
 */
public class YamlInstanceConfigurationSwapperUtil {
    
    private static final InstanceConfigurationYamlSwapper INSTANCE_SWAPPER = new InstanceConfigurationYamlSwapper();
    
    /**
     * marshal YamlInstanceConfiguration map to InstanceConfiguration map.
     *
     * @param yamlInstanceConfigurationMap YamlInstanceConfiguration map
     * @return InstanceConfiguration map
     */
    public static Map<String, InstanceConfiguration> marshal(final Map<String, YamlInstanceConfiguration> yamlInstanceConfigurationMap) {
        Map<String, InstanceConfiguration> result = new LinkedHashMap<>(yamlInstanceConfigurationMap.size(), 1);
        for (Map.Entry<String, YamlInstanceConfiguration> each : yamlInstanceConfigurationMap.entrySet()) {
            result.put(each.getKey(), INSTANCE_SWAPPER.swap(each.getValue()));
        }
        return result;
    }
}
