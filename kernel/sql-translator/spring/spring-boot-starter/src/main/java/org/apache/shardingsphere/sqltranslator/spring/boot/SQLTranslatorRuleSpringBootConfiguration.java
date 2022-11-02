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

package org.apache.shardingsphere.sqltranslator.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sqltranslator.spring.boot.condition.SQLTranslatorSpringBootCondition;
import org.apache.shardingsphere.sqltranslator.spring.boot.rule.YamlSQLTranslatorRuleSpringBootConfiguration;
import org.apache.shardingsphere.sqltranslator.yaml.config.YamlSQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.yaml.swapper.YamlSQLTranslatorRuleConfigurationSwapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * SQL translator rule configuration for spring boot.
 */
@Configuration
@EnableConfigurationProperties(YamlSQLTranslatorRuleSpringBootConfiguration.class)
@ConditionalOnClass(YamlSQLTranslatorRuleConfiguration.class)
@Conditional(SQLTranslatorSpringBootCondition.class)
@RequiredArgsConstructor
public class SQLTranslatorRuleSpringBootConfiguration {
    
    private final YamlSQLTranslatorRuleConfigurationSwapper swapper = new YamlSQLTranslatorRuleConfigurationSwapper();
    
    private final YamlSQLTranslatorRuleSpringBootConfiguration yamlConfig;
    
    /**
     * Create SQL translator rule configuration bean.
     *
     * @return SQL translator rule configuration
     */
    @Bean
    public RuleConfiguration sqlTranslatorRuleConfiguration() {
        return swapper.swapToObject(yamlConfig.getSqlTranslator());
    }
}
