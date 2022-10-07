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

package org.apache.shardingsphere.shadow.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.spring.boot.algorithm.ShadowAlgorithmProvidedBeanRegistry;
import org.apache.shardingsphere.shadow.spring.boot.condition.ShadowSpringBootCondition;
import org.apache.shardingsphere.shadow.spring.boot.rule.YamlShadowRuleSpringBootConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleAlgorithmProviderConfigurationSwapper;
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
 * Shadow rule configuration for spring boot.
 */
@Configuration
@EnableConfigurationProperties(YamlShadowRuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlShadowRuleConfiguration.class)
@Conditional(ShadowSpringBootCondition.class)
@RequiredArgsConstructor
public class ShadowRuleSpringBootConfiguration {
    
    private final YamlShadowRuleAlgorithmProviderConfigurationSwapper swapper = new YamlShadowRuleAlgorithmProviderConfigurationSwapper();
    
    private final YamlShadowRuleSpringBootConfiguration yamlConfig;
    
    /**
     * Shadow rule configuration.
     *
     * @param shadowAlgorithms shadow algorithms
     * @return shadow rule configuration
     */
    @Bean
    public RuleConfiguration shadowRuleConfiguration(final ObjectProvider<Map<String, ShadowAlgorithm>> shadowAlgorithms) {
        AlgorithmProvidedShadowRuleConfiguration result = swapper.swapToObject(yamlConfig.getShadow());
        result.setShadowAlgorithms(createShadowAlgorithmMap(shadowAlgorithms));
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithmMap(final ObjectProvider<Map<String, ShadowAlgorithm>> shadowAlgorithms) {
        return Optional.ofNullable(shadowAlgorithms.getIfAvailable()).orElse(Collections.emptyMap());
    }
    
    /**
     * Shadow algorithm provided bean registry.
     *
     * @param environment environment
     * @return shadow algorithm provided bean registry
     */
    @Bean
    public static ShadowAlgorithmProvidedBeanRegistry shadowAlgorithmProvidedBeanRegistry(final Environment environment) {
        return new ShadowAlgorithmProvidedBeanRegistry(environment);
    }
}
