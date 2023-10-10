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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Example configuration validator.
 */
public final class YamlExampleConfigurationValidator {
    
    /**
     * Verify the entrance.
     *
     * @param config Yaml example configuration
     */
    public static void validate(final YamlExampleConfiguration config) {
        Map<String, List<String>> configMap = new HashMap<>(5, 1);
        configMap.put("modes", config.getModes());
        configMap.put("transactions", config.getTransactions());
        configMap.put("features", config.getFeatures());
        configMap.put("frameworks", config.getFrameworks());
        validateConfigurationValues(configMap);
        validateAccountConfigProperties(config.getProps());
    }
    
    private static void validateConfigurationValues(final Map<String, List<String>> configMap) {
        configMap.forEach((key, value) -> {
            YamlExampleConfigurationSupportedValue supportedValueEnum = YamlExampleConfigurationSupportedValue.of(key);
            Set<String> supportedValues = supportedValueEnum.getSupportedValues();
            value.forEach(each -> Preconditions.checkArgument(supportedValues.contains(each), getConfigValueErrorMessage(key, supportedValues, each)));
        });
    }
    
    private static void validateAccountConfigProperties(final Properties props) {
        Collection<String> accountConfigItems = Arrays.asList("host", "port", "username", "password");
        accountConfigItems.forEach(each -> Preconditions.checkArgument(null != props.get(each), getConfigItemErrorMessage(each)));
    }
    
    private static String getConfigValueErrorMessage(final String configItem, final Set<String> supportedValues, final String errorValue) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",it only supports:" + supportedValues.toString() + ",the currently configured value:" + errorValue;
    }
    
    private static String getConfigItemErrorMessage(final String configItem) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",the configuration item missed or its value is null";
    }
}
