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

package org.apache.shardingsphere.ha.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.ha.algorithm.config.AlgorithmProvidedHARuleConfiguration;
import org.apache.shardingsphere.ha.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.ha.spring.boot.algorithm.HAAlgorithmProvidedBeanRegistry;
import org.apache.shardingsphere.ha.spring.boot.rule.YamlHARuleSpringBootConfiguration;
import org.apache.shardingsphere.ha.spring.boot.condition.HASpringBootCondition;
import org.apache.shardingsphere.ha.yaml.config.YamlHARuleConfiguration;
import org.apache.shardingsphere.ha.yaml.swapper.HARuleAlgorithmProviderConfigurationYamlSwapper;
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
@EnableConfigurationProperties(YamlHARuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlHARuleConfiguration.class)
@Conditional(HASpringBootCondition.class)
@RequiredArgsConstructor
public class HARuleSpringbootConfiguration {
    
    private final HARuleAlgorithmProviderConfigurationYamlSwapper swapper = new HARuleAlgorithmProviderConfigurationYamlSwapper();
    
    private final YamlHARuleSpringBootConfiguration yamlConfig;
    
    /**
     * Replica query rule configuration for spring boot.
     *
     * @param loadBalanceAlgorithms load balance algorithms
     * @return replica query rule configuration
     */
    @Bean
    public RuleConfiguration haRuleConfiguration(final ObjectProvider<Map<String, ReplicaLoadBalanceAlgorithm>> loadBalanceAlgorithms) {
        AlgorithmProvidedHARuleConfiguration result = swapper.swapToObject(yamlConfig.getHa());
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
    public static HAAlgorithmProvidedBeanRegistry haAlgorithmProvidedBeanRegistry(final Environment environment) {
        return new HAAlgorithmProvidedBeanRegistry(environment);
    }
}
