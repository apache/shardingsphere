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

package org.apache.shardingsphere.example.generator.core.yaml.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Example configuration validator.
 */
public class YamlExampleConfigurationValidator {
    
    /**
     * Verify the entrance.
     *
     * @param configuration configuration
     */
    public void validate(final YamlExampleConfiguration configuration) {
        Map<String, ConfigurationData> valueMap = new HashMap<>();
        valueMap.put("products", new ConfigurationData(configuration.getProducts(), Sets.newHashSet("jdbc", "proxy")));
        valueMap.put("modes", new ConfigurationData(configuration.getModes(), Sets.newHashSet("memory", "proxy", "cluster-zookeeper", "cluster-etcd", "standalone-file")));
        valueMap.put("transactions", new ConfigurationData(configuration.getTransactions(), Sets.newHashSet("local")));
        valueMap.put("features", new ConfigurationData(configuration.getFeatures(), Sets.newHashSet("shadow", "sharding", "readwrite-splitting", "encrypt", "db-discovery")));
        valueMap.put("frameworks", new ConfigurationData(configuration.getFrameworks(), Sets.newHashSet("jdbc", "spring-boot-starter-jdbc", "spring-boot-starter-jpa", "spring-boot-starter-mybatis", "spring-namespace-jdbc", "spring-namespace-jpa", "spring-namespace-mybatis")));
        Properties props = configuration.getProps();
        valueMap.put("host", new ConfigurationData(props.get("host") != null ? Lists.newArrayList(props.get("host").toString()) : null, Sets.newHashSet()));
        valueMap.put("port", new ConfigurationData(props.get("port") != null ? Lists.newArrayList(props.get("port").toString()) : null, Sets.newHashSet()));
        valueMap.put("username", new ConfigurationData(props.get("username") != null ? Lists.newArrayList(props.get("username").toString()) : null, Sets.newHashSet()));
        valueMap.put("password", new ConfigurationData(props.get("password") != null ? Lists.newArrayList(props.get("password").toString()) : null, Sets.newHashSet()));
        validateConfigurationValues(valueMap);
    }
    
    private void validateConfigurationValues(final Map<String, ConfigurationData> valueMap) {
        valueMap.forEach((key, value) -> {
            List<String> configuredValues = value.getConfiguredValues();
            Set<String> supportedValues = value.getSupportedValues();
            Preconditions.checkArgument(configuredValues != null && !configuredValues.isEmpty(), getConfigItemErrorMessage(key));
            if (!supportedValues.isEmpty()) {
                configuredValues.stream().forEach(v -> Preconditions.checkArgument(supportedValues.contains(v), getConfigValueErrorMessage(key, supportedValues, v)));
            }
        });
    }
    
    private String getConfigValueErrorMessage(final String configItem, final Set<String> supportedValues, final String errorValue) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",it only supports:" + supportedValues.toString() + ",the currently configured value:" + errorValue;
    }
    
    private String getConfigItemErrorMessage(final String configItem) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",the configuration item is misspelled or its value is null";
    }
    
    @Getter
    @Setter
    private class ConfigurationData {
        
        private List<String> configuredValues;
        
        private Set<String> supportedValues;
        
        ConfigurationData(final List<String> configuredValues, final Set<String> supportedValues) {
            this.configuredValues = configuredValues;
            this.supportedValues = supportedValues;
        }
    }
}
