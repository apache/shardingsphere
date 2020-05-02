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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.rule.ProxyUser;
import org.apache.shardingsphere.encrypt.api.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationLoggerTest {
    
    @Mock
    private Logger log;
    
    @Before
    public void setLog() throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigurationLogger.class.getDeclaredField("log");
        setFinalStatic(field, log);
    }
    
    private void setFinalStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
    
    @Test
    public void assertLogShardingRuleConfiguration() {
        String yaml = "tables:\n  user:\n    actualDataNodes: ds_${0}.user_${0..1}\n    logicTable: user\n    tableStrategy:\n      none: ''\n";
        assertLogInfo(ShardingRuleConfiguration.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getShardingRuleConfiguration());
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("user", "ds_${0}.user_${0..1}");
        tableRuleConfiguration.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        result.getTableRuleConfigs().add(tableRuleConfiguration);
        return result;
    }
    
    @Test
    public void assertLogMasterSlaveRuleConfiguration() {
        String yaml = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
        assertLogInfo(MasterSlaveRuleConfiguration.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getMasterSlaveRuleConfiguration());
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        return new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"));
    }
    
    @Test
    public void assertLogEncryptRuleConfiguration() {
        String yaml = "encryptors:\n" + "  encryptor_aes:\n" + "    props:\n" + "      aes.key.value: 123456abc\n" + "    type: aes\n"
                + "tables:\n" + "  t_encrypt:\n" + "    columns:\n" + "      user_id:\n" + "        assistedQueryColumn: user_assisted\n"
                + "        cipherColumn: user_encrypt\n" + "        encryptor: encryptor_aes\n" + "        plainColumn: user_decrypt\n";
        assertLogInfo(EncryptRuleConfiguration.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getEncryptRuleConfiguration());
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        Properties properties = new Properties();
        properties.put("aes.key.value", "123456abc");
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("aes", properties);
        EncryptTableRuleConfiguration tableRuleConfiguration =
                new EncryptTableRuleConfiguration(Collections.singletonMap("user_id", new EncryptColumnRuleConfiguration("user_decrypt", "user_encrypt", "user_assisted", "encryptor_aes")));
        return new EncryptRuleConfiguration(ImmutableMap.of("encryptor_aes", encryptorRuleConfiguration), ImmutableMap.of("t_encrypt", tableRuleConfiguration));
    }
    
    @Test
    public void assertLogRuleConfigurationWithEncryptRuleConfiguration() {
        String yaml = "encryptors:\n" + "  encryptor_aes:\n" + "    props:\n" + "      aes.key.value: 123456abc\n" + "    type: aes\n"
            + "tables:\n" + "  t_encrypt:\n" + "    columns:\n" + "      user_id:\n" + "        assistedQueryColumn: user_assisted\n"
            + "        cipherColumn: user_encrypt\n" + "        encryptor: encryptor_aes\n" + "        plainColumn: user_decrypt\n";
        assertLogInfo(EncryptRuleConfiguration.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getEncryptRuleConfiguration());
    }
    
    @Test
    public void assertLogRuleConfigurationWithShardingRuleConfiguration() {
        String yaml = "tables:\n  user:\n    actualDataNodes: ds_${0}.user_${0..1}\n    logicTable: user\n    tableStrategy:\n      none: ''\n";
        assertLogInfo(ShardingRuleConfiguration.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getShardingRuleConfiguration());
    }
    
    @Test
    public void assertLogRuleConfigurationWithMasterSlaveRuleConfiguration() {
        String yaml = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
        assertLogInfo(MasterSlaveRuleConfiguration.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getMasterSlaveRuleConfiguration());
    }
    
    @Test
    public void assertLogAuthenticationConfiguration() {
        String yaml = "users:\n" + "  root:\n" + "    authorizedSchemas: sharding_db\n" + "    password: '123456'\n";
        assertLogInfo(Authentication.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getAuthentication());
    }
    
    private Authentication getAuthentication() {
        Authentication result = new Authentication();
        result.getUsers().put("root", new ProxyUser("123456", Collections.singletonList("sharding_db")));
        return result;
    }
    
    @Test
    public void assertLogProperties() {
        String yaml = "sql.simple: 'true'\n" + "sql.show: 'true'\n";
        assertLogInfo(Properties.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getProperties());
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        result.put(ConfigurationPropertyKey.SQL_SIMPLE.getKey(), Boolean.TRUE.toString());
        return result;
    }
    
    private void assertLogInfo(final String type, final String logContent) {
        doAnswer(invocationOnMock -> {
            assertThat(invocationOnMock.getArgument(1).toString(), is(type));
            assertThat(invocationOnMock.getArgument(2).toString(), is(logContent));
            return null;
        }).when(log).info(anyString(), anyString(), anyString());
    }
}
