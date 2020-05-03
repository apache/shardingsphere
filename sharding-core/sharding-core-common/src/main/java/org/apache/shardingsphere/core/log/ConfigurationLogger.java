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

package org.apache.shardingsphere.core.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.representer.processor.ShardingTupleProcessorFactory;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.Properties;

/**
 * Configuration logger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ConfigurationLogger {
    
    /**
     * Log rule configuration.
     *
     * @param ruleConfigurations rule configurations
     */
    public static void log(final Collection<RuleConfiguration> ruleConfigurations) {
        for (RuleConfiguration each : ruleConfigurations) {
            log(each);
        }
    }
    
    /**
     * Log rule configuration.
     *
     * @param ruleConfiguration rule configuration
     */
    public static void log(final RuleConfiguration ruleConfiguration) {
        if (null == ruleConfiguration) {
            return;
        }
        if (ruleConfiguration instanceof ShardingRuleConfiguration) {
            log((ShardingRuleConfiguration) ruleConfiguration);
        } else if (ruleConfiguration instanceof MasterSlaveRuleConfiguration) {
            log((MasterSlaveRuleConfiguration) ruleConfiguration);
        } else if (ruleConfiguration instanceof EncryptRuleConfiguration) {
            log((EncryptRuleConfiguration) ruleConfiguration);
        } else if (ruleConfiguration instanceof ShadowRuleConfiguration) {
            log((ShadowRuleConfiguration) ruleConfiguration);
        }
    }
    
    private static void log(final ShardingRuleConfiguration shardingRuleConfiguration) {
        if (null != shardingRuleConfiguration) {
            log(shardingRuleConfiguration.getClass().getSimpleName(),
                YamlEngine.marshal(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfiguration), ShardingTupleProcessorFactory.newInstance()));
        }
    }
    
    private static void log(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        if (null != masterSlaveRuleConfiguration) {
            log(masterSlaveRuleConfiguration.getClass().getSimpleName(), YamlEngine.marshal(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfiguration)));
        }
    }
    
    private static void log(final EncryptRuleConfiguration encryptRuleConfiguration) {
        if (null != encryptRuleConfiguration) {
            log(encryptRuleConfiguration.getClass().getSimpleName(), YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swap(encryptRuleConfiguration)));
        }
    }

    private static void log(final ShadowRuleConfiguration shadowRuleConfiguration) {
        if (null != shadowRuleConfiguration) {
            log(shadowRuleConfiguration.getClass().getSimpleName(), YamlEngine.marshal(new ShadowRuleConfigurationYamlSwapper().swap(shadowRuleConfiguration)));
        }
    }
    
    /**
     * Log authentication configuration.
     *
     * @param authenticationConfiguration authentication configuration
     */
    public static void log(final Authentication authenticationConfiguration) {
        if (null != authenticationConfiguration) {
            log(authenticationConfiguration.getClass().getSimpleName(), YamlEngine.marshal(new AuthenticationYamlSwapper().swap(authenticationConfiguration)));
        }
    }
    
    /**
     * Log properties.
     *
     * @param properties properties
     */
    public static void log(final Properties properties) {
        if (null != properties) {
            log(properties.getClass().getSimpleName(), YamlEngine.marshal(properties));
        }
    }
    
    private static void log(final String type, final String logContent) {
        log.info("{}:\n{}", type, logContent);
    }
}
