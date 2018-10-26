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

import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingsphere.orchestration.internal.yaml.representer.DefaultConfigurationRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

/**
 * Sharding configuration converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingConfigurationConverter {
    
    private static final Yaml YAML = new Yaml(new DefaultConfigurationRepresenter());
    
    /**
     * Convert sharding rule configuration to yaml string.
     *
     * @param shardingRuleConfig sharding rule configuration
     * @return sharding rule configuration string
     */
    public static String shardingRuleConfigToYaml(final ShardingRuleConfiguration shardingRuleConfig) {
        return YAML.dumpAsMap(new YamlShardingRuleConfiguration(shardingRuleConfig));
    }
    
    /**
     * Convert sharding rule configuration string to sharding rule configuration.
     *
     * @param shardingRuleConfigYamlString sharding rule configuration string
     * @return sharding rule configuration
     */
    public static ShardingRuleConfiguration shardingRuleConfigFromYaml(final String shardingRuleConfigYamlString) {
        return YAML.loadAs(shardingRuleConfigYamlString, YamlShardingRuleConfiguration.class).getShardingRuleConfiguration();
    }
}
