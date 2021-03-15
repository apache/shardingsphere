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
import org.apache.shardingsphere.ha.spring.boot.condition.HASpringBootCondition;
import org.apache.shardingsphere.ha.spring.boot.rule.YamlHARuleSpringBootConfiguration;
import org.apache.shardingsphere.ha.yaml.config.YamlHARuleConfiguration;
import org.apache.shardingsphere.ha.yaml.swapper.HARuleAlgorithmProviderConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Rule spring boot configuration for HA.
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
     * HA rule configuration for spring boot.
     *
     * @return HA rule configuration
     */
    @Bean
    public RuleConfiguration haRuleConfiguration() {
        return swapper.swapToObject(yamlConfig.getHa());
    }
}
