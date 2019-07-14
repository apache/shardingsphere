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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.rule.ProxyUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationLoggerTest {

    @Mock
    private Logger log;

    @Before
    public void setLog() throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigurationLogger.class.getDeclaredField("log");
        setFinalStatic(field, log);
    }

    private static void setFinalStatic(final Field field, final Object newValue)
        throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    public void assertEqualsWithLogInfo(final String base, final String yamlStr) {
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
    public void logPropsConfiguration() {
        Properties properties = new Properties();
        properties.put("masterDataSourceName", "master_ds");
        properties.put("slaveDataSourceNames", Arrays.asList("slave_ds_0", "slave_ds_1"));

        String base = "Properties";
        String yamlStr =
            "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n" + "masterDataSourceName: master_ds\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log(properties);
    }

    @Test
    public void logEncryptRuleConfiguration() {
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.put("aes.key.value", "123456abc");
        EncryptorRuleConfiguration encryptorRuleConfiguration =
            new EncryptorRuleConfiguration("aes", "user.user_name", properties);
        encryptRuleConfiguration.getEncryptorRuleConfigs().put("encryptor_aes", encryptorRuleConfiguration);

        String base = "EncryptRuleConfiguration";
        String yamlStr = "encryptors:\n" + "  encryptor_aes:\n" + "    assistedQueryColumns: ''\n" + "    props:\n"
            + "      aes.key.value: 123456abc\n" + "    qualifiedColumns: user.user_name\n" + "    type: aes\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log(encryptRuleConfiguration);
    }

    @Test
    public void logShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("user", "ds_${0}.user_${0..1}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);

        String base = "ShardingRuleConfiguration";
        String yamlStr =
            "tables:\n" + "  user:\n" + "    actualDataNodes: ds_${0}.user_${0..1}\n" + "    logicTable: user\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log(shardingRuleConfiguration);
    }

    @Test
    public void logMasterSlaveRuleConfiguration() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration =
            new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"));

        String base = "MasterSlaveRuleConfiguration";
        String yamlStr = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n"
            + "- slave_ds_0\n" + "- slave_ds_1\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log(masterSlaveRuleConfiguration);
    }

    @Test
    public void logRuleConfiguration(){
        String base, yamlStr;
        // EncryptRuleConfiguration
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.put("aes.key.value", "123456abc");
        EncryptorRuleConfiguration encryptorRuleConfiguration =
            new EncryptorRuleConfiguration("aes", "user.user_name", properties);
        encryptRuleConfiguration.getEncryptorRuleConfigs().put("encryptor_aes", encryptorRuleConfiguration);

        base = "EncryptRuleConfiguration";
        yamlStr = "encryptors:\n" + "  encryptor_aes:\n" + "    assistedQueryColumns: ''\n" + "    props:\n"
            + "      aes.key.value: 123456abc\n" + "    qualifiedColumns: user.user_name\n" + "    type: aes\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log((RuleConfiguration) encryptRuleConfiguration);

        // ShardingRuleConfiguration
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("user", "ds_${0}.user_${0..1}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);

        base = "ShardingRuleConfiguration";
        yamlStr =
            "tables:\n" + "  user:\n" + "    actualDataNodes: ds_${0}.user_${0..1}\n" + "    logicTable: user\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log((RuleConfiguration) shardingRuleConfiguration);

        // MasterSlaveRuleConfiguration
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration =
            new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"));

        base = "MasterSlaveRuleConfiguration";
        yamlStr = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n"
            + "- slave_ds_0\n" + "- slave_ds_1\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log((RuleConfiguration) masterSlaveRuleConfiguration);
    }

    @Test
    public void logAuthenticationConfiguration() {
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", new ProxyUser("123456", Collections.singletonList("sharding_db")));

        String base = "Authentication";
        String yamlStr = "users:\n" + "  root:\n" + "    authorizedSchemas: sharding_db\n" + "    password: '123456'\n";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log(authentication);
    }

    @Test
    public void log() {
        String base = "base";
        String yamlStr = "yamlStr";
        assertEqualsWithLogInfo(base, yamlStr);

        ConfigurationLogger.log(base, yamlStr);
    }

}