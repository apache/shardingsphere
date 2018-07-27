/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.internal.yaml.converter;

import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.yaml.representer.DefaultConfigurationRepresenter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.representer.MasterSlaveConfigurationRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sharding configuration converter.
 *
 * @author panjuan
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MasterSlaveConfigurationConverter {
    
    private static final Yaml YAML = new Yaml(new DefaultConfigurationRepresenter());
    
    /**
     * Convert masterSlaveRuleConfiguration to yaml string.
     *
     * @param masterSlaveRuleConfiguration master slave rule configuration
     * @return master slave rule configuration string
     */
    public static String masterSlaveRuleConfigToYaml(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        Yaml yaml = new Yaml(new MasterSlaveConfigurationRepresenter());
        YamlMasterSlaveRuleConfiguration yamlMasterSlaveRuleConfiguration =
                new YamlMasterSlaveRuleConfiguration(masterSlaveRuleConfiguration, new HashMap<String, Object>(), new Properties());
        return yaml.dumpAsMap(yamlMasterSlaveRuleConfiguration);
    }
    
    /**
     * Convert master slave rule configuration string to master slave rule configuration.
     *
     * @param masterSlaveRuleConfigYamlString master slave rule configuration string
     * @return master slave rule configuration
     */
    public static MasterSlaveRuleConfiguration masterSlaveRuleConfigFromYaml(final String masterSlaveRuleConfigYamlString) {
        return YAML.loadAs(masterSlaveRuleConfigYamlString, YamlMasterSlaveRuleConfiguration.class).getMasterSlaveRuleConfiguration();
    }
    
    /**
     * Convert config map to yaml string.
     *
     * @param configMap config map
     * @return config map string
     */
    public static String configMapToYaml(final Map<String, Object> configMap) {
        return YAML.dumpAsMap(configMap);
    }
    
    /**
     * Convert config map string to config map.
     *
     * @param configMapYamlString config map string
     * @return config map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> configMapFromYaml(final String configMapYamlString) {
        return (Map<String, Object>) YAML.load(configMapYamlString);
    }
    
    /**
     * Convert properties to properties string.
     *
     * @param props properties
     * @return properties string
     */
    public static String propertiesToYaml(final Properties props) {
        return YAML.dumpAsMap(props);
    }
    
    /**
     * Convert properties yaml string to properties.
     *
     * @param propertiesYamlString properties yaml string
     * @return properties
     */
    public static Properties propertiesFromYaml(final String propertiesYamlString) {
        return YAML.loadAs(propertiesYamlString, Properties.class);
    }
}
