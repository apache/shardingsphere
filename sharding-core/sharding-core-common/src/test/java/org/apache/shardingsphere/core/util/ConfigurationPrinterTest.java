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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.junit.Test;

public class ConfigurationPrinterTest {

    @Test
    public void printMapConfiguration() {
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put("masterDataSourceName", "master_ds");
        configurationMap.put("slaveDataSourceNames", Arrays.asList("slave_ds_0", "slave_ds_1"));

        ConfigurationPrinter.printConfiguration("masterSlaveRule", configurationMap);
    }

    @Test
    public void printPropsConfiguration() {
        Properties properties = new Properties();
        properties.put("masterDataSourceName", "master_ds");
        properties.put("slaveDataSourceNames", Arrays.asList("slave_ds_0", "slave_ds_1"));

        ConfigurationPrinter.printConfiguration("masterSlaveRule", properties);
    }

    @Test
    public void printEncryptRuleConfiguration() {
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.put("aes.key.value", "123456abc");
        EncryptorRuleConfiguration encryptorRuleConfiguration =
            new EncryptorRuleConfiguration("aes", "user.user_name", properties);
        encryptRuleConfiguration.getEncryptorRuleConfigs().put("encryptor_aes", encryptorRuleConfiguration);

        ConfigurationPrinter.printConfiguration("encryptRule", encryptRuleConfiguration);
    }

    @Test
    public void printShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("user", "ds_${0}.user_${0..1}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);

        ConfigurationPrinter.printConfiguration("shardingRule", shardingRuleConfiguration);
    }

    @Test
    public void print() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration =
            new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"));

        ConfigurationPrinter.printConfiguration("masterSlaveRule", masterSlaveRuleConfiguration);
    }

}