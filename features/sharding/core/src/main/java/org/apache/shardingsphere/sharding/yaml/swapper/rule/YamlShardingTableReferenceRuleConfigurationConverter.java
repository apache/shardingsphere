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

package org.apache.shardingsphere.sharding.yaml.swapper.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * YAML sharding table reference rule configuration converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlShardingTableReferenceRuleConfigurationConverter {
    
    private static final int GENERATE_NAME_LENGTH = 16;
    
    /**
     * Convert to YAML String configuration.
     * 
     * @param data to be converted
     * @return String configuration
     */
    public static String convertToYamlString(final ShardingTableReferenceRuleConfiguration data) {
        return String.format("%s:%s", data.getName(), data.getReference());
    }
    
    /**
     * Convert from YAML configuration to object.
     * 
     * @param referenceConfig reference config
     * @return converted object
     */
    public static ShardingTableReferenceRuleConfiguration convertToObject(final String referenceConfig) {
        return referenceConfig.contains(":") ? convertYamlConfigurationWithName(referenceConfig) : convertYamlConfigurationWithoutName(referenceConfig);
    }
    
    private static ShardingTableReferenceRuleConfiguration convertYamlConfigurationWithName(final String referenceConfig) {
        String name = referenceConfig.substring(0, referenceConfig.indexOf(':'));
        String reference = referenceConfig.substring(referenceConfig.indexOf(':') + 1);
        return new ShardingTableReferenceRuleConfiguration(name, reference);
    }
    
    private static ShardingTableReferenceRuleConfiguration convertYamlConfigurationWithoutName(final String referenceConfig) {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        String name = new UUID(threadLocalRandom.nextLong(), threadLocalRandom.nextLong()).toString().replace("-", "").substring(0, GENERATE_NAME_LENGTH);
        return new ShardingTableReferenceRuleConfiguration(name, referenceConfig);
    }
}
