/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.shardingsphere.core.util;

import java.util.Map;
import java.util.Properties;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration printer class.
 *
 * @author sunbufu
 */
public class ConfigurationPrinter {

    private static Logger log = LoggerFactory.getLogger(ConfigurationPrinter.class);

    /**
     * set ConfigurationPrinter's log.
     * 
     * @param log new log
     */
    public static void setLog(final Logger log) {
        ConfigurationPrinter.log = log;
    }

    /**
     * print properties configuration.
     *
     * @param base base node name
     * @param propsConfiguration properties configuration
     */
    public static void printConfiguration(final String base, final Properties propsConfiguration) {
        print(base, YamlEngine.marshal(propsConfiguration));
    }

    /**
     * print EncryptRuleConfiguration.
     *
     * @param base base node name.
     * @param encryptRuleConfiguration encryptRule configuration
     */
    public static void printConfiguration(final String base, final EncryptRuleConfiguration encryptRuleConfiguration) {
        print(base, YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swap(encryptRuleConfiguration)));
    }

    /**
     * print ShardingRuleConfiguration.
     *
     * @param base base node name
     * @param shardingRuleConfiguration shardingRule configuration
     */
    public static void printConfiguration(final String base,
        final ShardingRuleConfiguration shardingRuleConfiguration) {
        print(base, YamlEngine.marshal(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfiguration)));
    }

    /**
     * print MasterSlaveRuleConfiguration.
     *
     * @param base base node name
     * @param masterSlaveRuleConfiguration masterSlaveRule configuration
     */
    public static void printConfiguration(final String base,
        final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        print(base,
            YamlEngine.marshal(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfiguration)));
    }

    /**
     * print map configuration.
     *
     * @param base base node name
     * @param mapConfiguration map configuration
     */
    public static void printConfiguration(final String base, final Map<String, ?> mapConfiguration) {
        print(base, YamlEngine.marshal(mapConfiguration));
    }

    /**
     * print configuration log.
     *
     * @param base base node name
     * @param yamlStr yaml string
     */
    public static void print(final String base, final String yamlStr) {
        log.info("{}\n{}", base, yamlStr);
    }

}
