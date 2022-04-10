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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Example configuration validator.
 */
public final class YamlExampleConfigurationValidator {

    /**
     * Verify the entrance.
     *
     * @param configuration configuration
     */
    public static void validate(final YamlExampleConfiguration configuration) {
        Map<String, List<String>> configurationMap = Maps.newHashMap();
        configurationMap.put("products", configuration.getProducts());
        configurationMap.put("modes", configuration.getModes());
        configurationMap.put("transactions", configuration.getTransactions());
        configurationMap.put("features", configuration.getFeatures());
        configurationMap.put("frameworks", configuration.getFrameworks());
        validateConfigurationValues(configurationMap);
        validateAccountConfigProps(configuration.getProps());
    }

    private static void validateConfigurationValues(final Map<String, List<String>> configurationMap) {
        configurationMap.forEach((configItem, configValues) -> {
            YamlExampleConfigurationSupportedValue supportedValueEnum = YamlExampleConfigurationSupportedValue.of(configItem);
            Set<String> supportedValues = supportedValueEnum.getSupportedValues();
            configValues.stream().forEach(v -> Preconditions.checkArgument(supportedValues.contains(v), getConfigValueErrorMessage(configItem, supportedValues, v)));
        });
    }

    private static void validateAccountConfigProps(final Properties props) {
        List<String> accountConfigItemList = Lists.newArrayList("host", "port", "username", "password");
        accountConfigItemList.forEach(item -> Preconditions.checkArgument(props.get(item) != null, getConfigItemErrorMessage(item)));
    }

    private static String getConfigValueErrorMessage(final String configItem, final Set<String> supportedValues, final String errorValue) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",it only supports:" + supportedValues.toString() + ",the currently configured value:" + errorValue;
    }

    private static String getConfigItemErrorMessage(final String configItem) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",the configuration item missed or its value is null";
    }
}
