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

package org.apache.shardingsphere.replication.primaryreplica.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.spi.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.replication.primaryreplica.spring.boot.algorithm.MasterSlaveAlgorithmProvidedBeanRegistry;
import org.apache.shardingsphere.replication.primaryreplica.spring.boot.condition.MasterSlaveSpringBootCondition;
import org.apache.shardingsphere.replication.primaryreplica.spring.boot.rule.YamlMasterSlaveRuleSpringBootConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.swapper.MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(YamlMasterSlaveRuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlMasterSlaveRuleConfiguration.class)
@Conditional(MasterSlaveSpringBootCondition.class)
@RequiredArgsConstructor
public class MasterSlaveRuleSpringbootConfiguration {
    
    private final MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper swapper = new MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper();
    
    private final YamlMasterSlaveRuleSpringBootConfiguration yamlConfig;
    
    /**
     * Master slave rule configuration for spring boot.
     *
     * @param loadBalanceAlgorithms load balance algorithms
     * @return master slave rule configuration
     */
    @Bean
    public RuleConfiguration masterSlaveRuleConfiguration(final ObjectProvider<Map<String, MasterSlaveLoadBalanceAlgorithm>> loadBalanceAlgorithms) {
        AlgorithmProvidedMasterSlaveRuleConfiguration result = swapper.swapToObject(yamlConfig.getMasterSlave());
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
