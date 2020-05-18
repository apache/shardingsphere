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

package org.apache.shardingsphere.driver.spring.boot.rule;

import org.apache.shardingsphere.driver.spring.boot.condition.EncryptSpringBootCondition;
import org.apache.shardingsphere.driver.spring.boot.condition.MasterSlaveSpringBootCondition;
import org.apache.shardingsphere.driver.spring.boot.condition.ShadowSpringBootCondition;
import org.apache.shardingsphere.driver.spring.boot.condition.ShardingSpringBootCondition;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Spring boot rules configuration.
 */
@Configuration
public class SpringBootRulesConfiguration {
    
    /**
     * Sharding YAML rule configuration.
     *
     * @return YAML rule configuration
     */
    @Bean
    @ConditionalOnClass(YamlShardingRuleConfiguration.class)
    @ConfigurationProperties(prefix = "spring.shardingsphere.rules.sharding")
    @Conditional(ShardingSpringBootCondition.class)
    public YamlRuleConfiguration sharding() {
        return new YamlShardingRuleConfiguration();
    }
    
    /**
     * Master slave YAML rule configuration.
     *
     * @return YAML rule configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.shardingsphere.rules.master-slave")
    @ConditionalOnClass(YamlMasterSlaveRuleConfiguration.class)
    @Conditional(MasterSlaveSpringBootCondition.class)
    public YamlRuleConfiguration masterSlave() {
        return new YamlMasterSlaveRuleConfiguration();
    }
    
    /**
     * Encrypt YAML rule configuration.
     *
     * @return YAML rule configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.shardingsphere.rules.encrypt")
    @ConditionalOnClass(YamlEncryptRuleConfiguration.class)
    @Conditional(EncryptSpringBootCondition.class)
    public YamlRuleConfiguration encrypt() {
        return new YamlEncryptRuleConfiguration();
    }
    
    /**
     * Shadow YAML rule configuration.
     *
     * @return YAML rule configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.shardingsphere.rules.shadow")
    @ConditionalOnClass(YamlShadowRuleConfiguration.class)
    @Conditional(ShadowSpringBootCondition.class)
    public YamlRuleConfiguration shadow() {
        return new YamlShadowRuleConfiguration();
    }
    
    /**
     * Default YAML rule configuration.
     *
     * @return YAML rule configuration
     */
    @Bean
    @ConditionalOnMissingBean(YamlRuleConfiguration.class)
    public YamlRuleConfiguration noRules() {
        return () -> DefaultRuleConfiguration.class;
    }
}
