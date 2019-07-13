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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

public class ConfigurationPrinterTest {

    private Logger log;

    @Before
    public void setLog(){
        log = mock(Logger.class);
        ConfigurationPrinter.setLog(log);
    }

    public void assertEqualsWithLogInfo(final String base, final String yamlStr){
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) {
                Assert.assertEquals(base, invocationOnMock.getArgument(1));
                Assert.assertEquals(yamlStr, invocationOnMock.getArgument(2));
                return null;
            }
        }).when(log).info(anyString(), anyString(), anyString());
    }

    @Test
    public void printMapConfiguration() {
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put("masterDataSourceName", "master_ds");
        configurationMap.put("slaveDataSourceNames", Arrays.asList("slave_ds_0", "slave_ds_1"));

        String base = "masterSlaveRule";
        String yamlStr = "slaveDataSourceNames:\n" +
            "- slave_ds_0\n" +
            "- slave_ds_1\n" +
            "masterDataSourceName: master_ds\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationPrinter.printConfiguration(base, configurationMap);
    }

    @Test
    public void printPropsConfiguration() {
        Properties properties = new Properties();
        properties.put("masterDataSourceName", "master_ds");
        properties.put("slaveDataSourceNames", Arrays.asList("slave_ds_0", "slave_ds_1"));

        String base = "masterSlaveRule";
        String yamlStr = "slaveDataSourceNames:\n" +
            "- slave_ds_0\n" +
            "- slave_ds_1\n" +
            "masterDataSourceName: master_ds\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationPrinter.printConfiguration(base, properties);
    }

    @Test
    public void printEncryptRuleConfiguration() {
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.put("aes.key.value", "123456abc");
        EncryptorRuleConfiguration encryptorRuleConfiguration =
            new EncryptorRuleConfiguration("aes", "user.user_name", properties);
        encryptRuleConfiguration.getEncryptorRuleConfigs().put("encryptor_aes", encryptorRuleConfiguration);

        String base = "encryptRule";
        String yamlStr = "encryptors:\n" +
            "  encryptor_aes:\n" +
            "    assistedQueryColumns: ''\n" +
            "    props:\n" +
            "      aes.key.value: 123456abc\n" +
            "    qualifiedColumns: user.user_name\n" +
            "    type: aes\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationPrinter.printConfiguration(base, encryptRuleConfiguration);
    }

    @Test
    public void printShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("user", "ds_${0}.user_${0..1}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);

        String base = "shardingRule";
        String yamlStr = "tables:\n" +
            "  user:\n" +
            "    actualDataNodes: ds_${0}.user_${0..1}\n" +
            "    logicTable: user\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationPrinter.printConfiguration(base, shardingRuleConfiguration);
    }

    @Test
    public void printMasterSlaveRuleConfiguration() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration =
            new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"));

        String base = "masterSlaveRule";
        String yamlStr = "masterDataSourceName: master_ds\n" +
            "name: ms_ds\n" +
            "slaveDataSourceNames:\n" +
            "- slave_ds_0\n" +
            "- slave_ds_1\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationPrinter.printConfiguration("masterSlaveRule", masterSlaveRuleConfiguration);
    }

    @Test
    public void print() {
        String base = "base";
        String yamlStr = "yamlStr";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationPrinter.print(base, yamlStr);
    }

}