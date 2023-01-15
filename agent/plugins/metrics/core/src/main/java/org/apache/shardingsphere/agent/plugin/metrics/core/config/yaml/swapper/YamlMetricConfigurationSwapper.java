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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.entity.YamlMetricConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        String id = yamlConfig.getId();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Metric ID can not be null.");
        String type = yamlConfig.getType();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "Metric type can not be null.");
        String help = yamlConfig.getHelp();
        List<String> labels = null == yamlConfig.getLabels() ? Collections.emptyList() : yamlConfig.getLabels();
        Map<String, Object> props = null == yamlConfig.getProps() ? Collections.emptyMap() : yamlConfig.getProps();
        return new MetricConfiguration(id, type, help, labels, props);
    }
}
