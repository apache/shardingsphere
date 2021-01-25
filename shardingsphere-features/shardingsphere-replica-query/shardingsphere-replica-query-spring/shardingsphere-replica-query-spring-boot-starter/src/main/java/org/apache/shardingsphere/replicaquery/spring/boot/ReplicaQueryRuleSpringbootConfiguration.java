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

package org.apache.shardingsphere.replicaquery.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.replicaquery.algorithm.config.AlgorithmProvidedReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replicaquery.spring.boot.algorithm.ReplicaQueryAlgorithmProvidedBeanRegistry;
import org.apache.shardingsphere.replicaquery.spring.boot.rule.YamlReplicaQueryRuleSpringBootConfiguration;
import org.apache.shardingsphere.replicaquery.spring.boot.condition.ReplicaQuerySpringBootCondition;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.swapper.ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapper;
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
 * Rule spring boot configuration for replica query.
 */
@Configuration
@EnableConfigurationProperties(YamlReplicaQueryRuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlReplicaQueryRuleConfiguration.class)
@Conditional(ReplicaQuerySpringBootCondition.class)
@RequiredArgsConstructor
public class ReplicaQueryRuleSpringbootConfiguration {
    
    private final ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapper swapper = new ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapper();
    
    private final YamlReplicaQueryRuleSpringBootConfiguration yamlConfig;
    
    /**
     * Replica query rule configuration for spring boot.
     *
     * @param loadBalanceAlgorithms load balance algorithms
     * @return replica query rule configuration
     */
    @Bean
    public RuleConfiguration replicaQueryRuleConfiguration(final ObjectProvider<Map<String, ReplicaLoadBalanceAlgorithm>> loadBalanceAlgorithms) {
        AlgorithmProvidedReplicaQueryRuleConfiguration result = swapper.swapToObject(yamlConfig.getReplicaQuery());
        Map<String, ReplicaLoadBalanceAlgorithm> balanceAlgorithmMap = Optional.ofNullable(loadBalanceAlgorithms.getIfAvailable()).orElse(Collections.emptyMap());
        result.setLoadBalanceAlgorithms(balanceAlgorithmMap);
        return result;
    }
    
    /**
     * Replica query algorithm provided bean registry.
     *
     * @param environment environment
     * @return replica query algorithm provided bean registry
     */
    @Bean
    public static ReplicaQueryAlgorithmProvidedBeanRegistry replicaQueryAlgorithmProvidedBeanRegistry(final Environment environment) {
        return new ReplicaQueryAlgorithmProvidedBeanRegistry(environment);
    }
}
