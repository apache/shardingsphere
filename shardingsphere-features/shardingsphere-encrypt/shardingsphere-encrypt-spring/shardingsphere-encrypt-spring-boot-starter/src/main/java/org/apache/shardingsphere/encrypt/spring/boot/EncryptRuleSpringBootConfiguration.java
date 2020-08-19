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

package org.apache.shardingsphere.encrypt.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spring.boot.algorithm.EncryptAlgorithmProvidedBeanRegistry;
import org.apache.shardingsphere.encrypt.spring.boot.condition.EncryptSpringBootCondition;
import org.apache.shardingsphere.encrypt.spring.boot.rule.YamlEncryptRuleSpringBootConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleAlgorithmProviderConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
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
 * Encrypt rule configuration for spring boot.
 */
@Configuration
@EnableConfigurationProperties(YamlEncryptRuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlEncryptRuleConfiguration.class)
@Conditional(EncryptSpringBootCondition.class)
@RequiredArgsConstructor
public class EncryptRuleSpringBootConfiguration {
    
    private final EncryptRuleAlgorithmProviderConfigurationYamlSwapper swapper = new EncryptRuleAlgorithmProviderConfigurationYamlSwapper();
    
    private final YamlEncryptRuleSpringBootConfiguration yamlConfig;
    
    /**
     * Encrypt rule configuration for spring boot.
     *
     * @param encryptors encryptors algorithm to map
     * @return encrypt rule configuration
     */
    @Bean
    public RuleConfiguration encryptRuleConfiguration(final ObjectProvider<Map<String, EncryptAlgorithm>> encryptors) {
        AlgorithmProvidedEncryptRuleConfiguration result = swapper.swapToObject(yamlConfig.getEncrypt());
        result.setEncryptors(Optional.ofNullable(encryptors.getIfAvailable()).orElse(Collections.emptyMap()));
        return result;
    }
    
    /**
     * Encrypt algorithm provided bean registry.
     *
     * @param environment environment
     * @return encrypt algorithm provided bean registry
     */
    @Bean
    public static EncryptAlgorithmProvidedBeanRegistry encryptAlgorithmProvidedBeanRegistry(final Environment environment) {
        return new EncryptAlgorithmProvidedBeanRegistry(environment);
    }
}

