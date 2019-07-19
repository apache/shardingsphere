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

package org.apache.shardingsphere.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;

import java.util.Properties;

/**
 * Configuration printer class.
 *
 * @author sunbufu
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationLogger {

    /**
     * log properties configuration.
     *
     * @param propsConfiguration properties configuration
     */
    public static void log(final Properties propsConfiguration) {
        if (null == propsConfiguration) {
            return;
        }
        log(propsConfiguration.getClass().getSimpleName(), YamlEngine.marshal(propsConfiguration));
    }

    /**
     * log EncryptRuleConfiguration.
     *
     * @param encryptRuleConfiguration encryptRule configuration
     */
    public static void log(final EncryptRuleConfiguration encryptRuleConfiguration) {
        if (null == encryptRuleConfiguration) {
            return;
        }
        log(encryptRuleConfiguration.getClass().getSimpleName(),
            YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swap(encryptRuleConfiguration)));
    }

    /**
     * log ruleConfiguration.
     *
     * @param ruleConfiguration ruleConfiguration
     */
    public static void log(final RuleConfiguration ruleConfiguration) {
        if (null == ruleConfiguration) {
            return;
        }
        if (ruleConfiguration instanceof ShardingRuleConfiguration) {
            ConfigurationLogger.log((ShardingRuleConfiguration) ruleConfiguration);
        } else if (ruleConfiguration instanceof MasterSlaveRuleConfiguration) {
            ConfigurationLogger.log((MasterSlaveRuleConfiguration) ruleConfiguration);
        } else if (ruleConfiguration instanceof EncryptRuleConfiguration) {
            ConfigurationLogger.log((EncryptRuleConfiguration) ruleConfiguration);
        }
    }

    /**
     * log ShardingRuleConfiguration.
     *
     * @param shardingRuleConfiguration shardingRule configuration
     */
    public static void log(final ShardingRuleConfiguration shardingRuleConfiguration) {
        if (null == shardingRuleConfiguration) {
            return;
        }
        log(shardingRuleConfiguration.getClass().getSimpleName(),
            YamlEngine.marshal(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfiguration)));
    }

    /**
     * log MasterSlaveRuleConfiguration.
     *
     * @param masterSlaveRuleConfiguration masterSlaveRule configuration
     */
    public static void log(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        if (null == masterSlaveRuleConfiguration) {
            return;
        }
        log(masterSlaveRuleConfiguration.getClass().getSimpleName(),
            YamlEngine.marshal(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfiguration)));
    }

    /**
     * log AuthenticationConfiguration.
     *
     * @param authenticationConfiguration authentication configuration
     */
    public static void log(final Authentication authenticationConfiguration) {
        if (null == authenticationConfiguration) {
            return;
        }
        log(authenticationConfiguration.getClass().getSimpleName(),
            YamlEngine.marshal(new AuthenticationYamlSwapper().swap(authenticationConfiguration)));
    }

    /**
     * log configuration log.
     *
     * @param base base node name
     * @param yamlStr yaml string
     */
    public static void log(final String base, final String yamlStr) {
        log.info("{}\n{}", base, yamlStr);
    }
}
