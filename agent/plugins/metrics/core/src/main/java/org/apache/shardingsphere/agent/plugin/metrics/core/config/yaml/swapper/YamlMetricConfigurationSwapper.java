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

package org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.entity.YamlMetricConfiguration;

import java.util.Collections;

/**
 * YAML metric configuration swapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlMetricConfigurationSwapper {
    
    /**
     * Swap from YAML metric configuration to metric configuration.
     * 
     * @param yamlConfig YAML metric configuration
     * @return metric configuration
     */
    public static MetricConfiguration swap(final YamlMetricConfiguration yamlConfig) {
        return new MetricConfiguration(yamlConfig.getId(), yamlConfig.getType(), yamlConfig.getHelp(),
                null == yamlConfig.getLabels() ? Collections.emptyList() : yamlConfig.getLabels(), null == yamlConfig.getProps() ? Collections.emptyMap() : yamlConfig.getProps());
    }
}
