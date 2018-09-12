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

import io.shardingsphere.core.api.config.ProxyServerConfiguration;
import io.shardingsphere.core.api.config.ProxySchemaRule;
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
     * @return Yaml string
     */
    public static String proxyRuleConfigToYaml(final Map<String, ProxySchemaRule> schemaShardingRuleMap) {
        return YAML.dumpAsMap(schemaShardingRuleMap);
    }
    
    /**
     * Convert yaml to schema sharding rule configuration map.
     *
     * @param schemaShardingRuleMapString String in yaml
     * @return schema sharding rule configuration map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ProxySchemaRule> proxyRuleConfigFromYaml(final String schemaShardingRuleMapString) {
        return (Map) YAML.load(schemaShardingRuleMapString);
    }
    
    /**
     * Convert proxy proxy server configuration to yaml string.
     *
     * @param serverConfiguration proxy proxy server configuration
     * @return Yaml string
     */
    public static String proxyServerConfigToYaml(final ProxyServerConfiguration serverConfiguration) {
        return YAML.dumpAsMap(serverConfiguration);
    }
    
    /**
     * Convert yaml to proxy proxy server configuration.
     *
     * @param serverConfigurationString String in yaml
     * @return proxy server configuration
     */
    public static ProxyServerConfiguration proxyServerConfigFromYaml(final String serverConfigurationString) {
        return YAML.loadAs(serverConfigurationString, ProxyServerConfiguration.class);
    }
}
