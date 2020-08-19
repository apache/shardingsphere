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

package org.apache.shardingsphere.masterslave.spring.boot;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.spring.boot.condition.MasterSlaveSpringBootCondition;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.swapper.MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Rule spring boot configuration for master-slave.
 */
@Configuration
@ConditionalOnClass(YamlMasterSlaveRuleConfiguration.class)
@Conditional(MasterSlaveSpringBootCondition.class)
public class MasterSlaveRuleSpringbootConfiguration {
    
    private final MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper swapper = new MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper();
    
    /**
     * YAML rule spring boot configuration for master-slave.
     *
     * @return YAML rule configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.shardingsphere.rules.master-slave")
    public YamlMasterSlaveRuleConfiguration masterSlaveConfig() {
        return new YamlMasterSlaveRuleConfiguration();
    }
    
    /**
     * Master slave rule configuration for spring boot.
     *
     * @param yamlRuleConfiguration YAML master slave rule configuration
     * @param loadBalanceAlgorithms load balance algorithms
     * @return master slave rule configuration
     */
    @Bean
    public RuleConfiguration masterSlaveRuleConfiguration(final YamlMasterSlaveRuleConfiguration yamlRuleConfiguration,
                                                          final ObjectProvider<Map<String, MasterSlaveLoadBalanceAlgorithm>> loadBalanceAlgorithms) {
        AlgorithmProvidedMasterSlaveRuleConfiguration result = swapper.swapToObject(yamlRuleConfiguration);
        Map<String, MasterSlaveLoadBalanceAlgorithm> balanceAlgorithmMap = Optional.ofNullable(loadBalanceAlgorithms.getIfAvailable()).orElse(Collections.emptyMap());
        result.setLoadBalanceAlgorithms(balanceAlgorithmMap);
        return result;
    }
    
    /**
     * Master slave algorithm provided bean registry.
     *
     * @param environment environment
     * @return Master slave algorithm provided bean registry
     */
    @Bean
    public static MasterSlaveAlgorithmProvidedBeanRegistry masterSlaveAlgorithmProvidedBeanRegistry(final Environment environment) {
        return new MasterSlaveAlgorithmProvidedBeanRegistry(environment);
    }
}

