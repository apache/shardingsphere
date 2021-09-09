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

package org.apache.shardingsphere.driver.jdbc.core.log;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rule configuration logger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class RuleConfigurationLogger {
    
    /**
     * Log encrypt rule configuration.
     *
     * @param ruleConfiguration rule configuration
     */
    public static void log(final RuleConfiguration ruleConfiguration) {
        if (null == ruleConfiguration) {
            return;
        }
        if (ruleConfiguration instanceof EncryptRuleConfiguration) {
            log((EncryptRuleConfiguration) ruleConfiguration);
        }
    }
    
    private static void log(final EncryptRuleConfiguration encryptRuleConfiguration) {
        if (null != encryptRuleConfiguration) {
            log(encryptRuleConfiguration.getClass().getSimpleName(), YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swapToYamlConfiguration(encryptRuleConfiguration)));
        }
    }
    
    private static void log(final String type, final String logContent) {
        log.info("{}:\n{}", type, logContent);
    }
}
