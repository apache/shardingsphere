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

package io.shardingsphere.orchestration.internal.yaml.converter;

import io.shardingsphere.orchestration.internal.yaml.representer.DefaultConfigurationRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Properties;

/**
 * Common configuration converter.
 *
 * @author panjuan
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConfigurationConverter {
    
    private static final Yaml YAML = new Yaml(new DefaultConfigurationRepresenter());
    
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
     * Convert properties to yaml string.
     *
     * @param props properties
     * @return yaml string
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
