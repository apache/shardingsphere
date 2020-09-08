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

package org.apache.shardingsphere.primaryreplica.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.spi.PrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.spring.boot.algorithm.PrimaryReplicaAlgorithmProvidedBeanRegistry;
import org.apache.shardingsphere.masterslave.spring.boot.condition.PrimaryReplicaSpringBootCondition;
import org.apache.shardingsphere.masterslave.spring.boot.rule.YamlPrimaryReplicaRuleSpringBootConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.swapper.MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper;
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
 * Rule spring boot configuration for primary-replica.
 */
@Configuration
@EnableConfigurationProperties(YamlPrimaryReplicaRuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlPrimaryReplicaRuleConfiguration.class)
@Conditional(PrimaryReplicaSpringBootCondition.class)
@RequiredArgsConstructor
public class PrimaryReplicaRuleSpringbootConfiguration {
    
    private final PrimaryReplicaRuleAlgorithmProviderConfigurationYamlSwapper swapper = new PrimaryReplicaRuleAlgorithmProviderConfigurationYamlSwapper();
    
    private final YamlPrimaryReplicaRuleSpringBootConfiguration yamlConfig;
    
    /**
     * Primary replica rule configuration for spring boot.
     *
     * @param loadBalanceAlgorithms load balance algorithms
     * @return primary replica rule configuration
     */
    @Bean
    public RuleConfiguration primaryReplicaRuleConfiguration(final ObjectProvider<Map<String, PrimaryReplicaLoadBalanceAlgorithm>> loadBalanceAlgorithms) {
        AlgorithmProvidedPrimaryReplicaRuleConfiguration result = swapper.swapToObject(yamlConfig.getPrimaryReplica());
        Map<String, PrimaryReplicaLoadBalanceAlgorithm> balanceAlgorithmMap = Optional.ofNullable(loadBalanceAlgorithms.getIfAvailable()).orElse(Collections.emptyMap());
        result.setLoadBalanceAlgorithms(balanceAlgorithmMap);
        return result;
    }
    
    /**
     * Primary replica algorithm provided bean registry.
     *
     * @param environment environment
     * @return Primary replica algorithm provided bean registry
     */
    @Bean
    public static PrimaryReplicaAlgorithmProvidedBeanRegistry primaryReplicaAlgorithmProvidedBeanRegistry(final Environment environment) {
        return new PrimaryReplicaAlgorithmProvidedBeanRegistry(environment);
    }
}
