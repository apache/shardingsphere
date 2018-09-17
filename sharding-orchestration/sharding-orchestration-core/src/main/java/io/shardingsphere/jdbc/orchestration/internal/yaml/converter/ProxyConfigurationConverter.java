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

import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.yaml.representer.DefaultConfigurationRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Proxy Config converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyConfigurationConverter {
    
    private static final Yaml YAML = new Yaml(new DefaultConfigurationRepresenter());
    
    /**
     * Convert schema sharding rule configuration map to yaml string.
     *
     * @param schemaShardingRuleMap Schema sharding rule map
     * @return yaml string
     */
    public static String proxyRuleConfigToYaml(final Map<String, YamlRuleConfiguration> schemaShardingRuleMap) {
        return YAML.dumpAsMap(schemaShardingRuleMap);
    }
    
    /**
     * Convert yaml to schema sharding rule configuration map.
     *
     * @param schemaRuleMapString string in yaml
     * @return schema rule configuration map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, YamlRuleConfiguration> proxyRuleConfigFromYaml(final String schemaRuleMapString) {
        return (Map) YAML.load(schemaRuleMapString);
    }
    
    /**
     * Convert server configuration to yaml string.
     *
     * @param serverConfig server configuration
     * @return yaml string
     */
    public static String proxyServerConfigToYaml(final YamlServerConfiguration serverConfig) {
        return YAML.dumpAsMap(serverConfig);
    }
    
    /**
     * Convert yaml to server configuration.
     *
     * @param serverConfigString string in yaml
     * @return server configuration
     */
    public static YamlServerConfiguration proxyServerConfigFromYaml(final String serverConfigString) {
        return YAML.loadAs(serverConfigString, YamlServerConfiguration.class);
    }
}
