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

package org.apache.shardingsphere.globallogicaltime.yaml.swapper;

import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.globallogicaltime.config.RedisConnectionOptionConfiguration;
import org.apache.shardingsphere.globallogicaltime.constant.GlobalLogicalTimeOrder;
import org.apache.shardingsphere.globallogicaltime.rule.builder.DefaultGlobalLogicalTimeRuleConfigurationBuilder;
import org.apache.shardingsphere.globallogicaltime.yaml.config.YamlGlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

/**
 * YAML global logical time rule configuration swapper.
 */
public class YamlGlobalLogicalTimeRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlGlobalLogicalTimeRuleConfiguration, GlobalLogicalTimeRuleConfiguration> {
    
    private final YamlRedisConnectionOptionConfigurationSwapper cacheOptionSwapper = new YamlRedisConnectionOptionConfigurationSwapper();
    
    @Override
    public String getRuleTagName() {
        return "GLOBAL_LOGICAL_TIME";
    }
    
    @Override
    public int getOrder() {
        return GlobalLogicalTimeOrder.ORDER;
    }
    
    @Override
    public Class<GlobalLogicalTimeRuleConfiguration> getTypeClass() {
        return GlobalLogicalTimeRuleConfiguration.class;
    }
    
    @Override
    public YamlGlobalLogicalTimeRuleConfiguration swapToYamlConfiguration(final GlobalLogicalTimeRuleConfiguration data) {
        YamlGlobalLogicalTimeRuleConfiguration result = new YamlGlobalLogicalTimeRuleConfiguration();
        result.setGlobalLogicalTimeEnabled(data.isGlobalLogicalTimeEnabled());
        result.setRedisOption(cacheOptionSwapper.swapToYamlConfiguration(data.getRedisOption()));
        return result;
    }
    
    @Override
    public GlobalLogicalTimeRuleConfiguration swapToObject(final YamlGlobalLogicalTimeRuleConfiguration yamlConfig) {
        RedisConnectionOptionConfiguration configuration = null == yamlConfig.getRedisOption()
                ? DefaultGlobalLogicalTimeRuleConfigurationBuilder.REDIS_CONNECTION_OPTION
                : cacheOptionSwapper.swapToObject(yamlConfig.getRedisOption());
        return new GlobalLogicalTimeRuleConfiguration(yamlConfig.isGlobalLogicalTimeEnabled(), configuration);
        
    }
}
