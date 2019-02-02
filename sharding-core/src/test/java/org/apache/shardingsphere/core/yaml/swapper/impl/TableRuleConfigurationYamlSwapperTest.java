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

package org.apache.shardingsphere.core.yaml.swapper.impl;

import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlEncryptorConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlKeyGeneratorConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TableRuleConfigurationYamlSwapperTest {
    
    @Mock
    private ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper;
    
    @Mock
    private KeyGeneratorConfigurationYamlSwapper keyGeneratorConfigurationYamlSwapper;
    
    @Mock
    private EncryptorConfigurationYamlSwapper encryptorConfigurationYamlSwapper;
    
    private TableRuleConfigurationYamlSwapper tableRuleConfigurationYamlSwapper = new TableRuleConfigurationYamlSwapper();
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSwapper("shardingStrategyConfigurationYamlSwapper", shardingStrategyConfigurationYamlSwapper);
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<ShardingStrategyConfiguration>any())).thenReturn(mock(YamlShardingStrategyConfiguration.class));
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlShardingStrategyConfiguration>any())).thenReturn(mock(ShardingStrategyConfiguration.class));
        setSwapper("keyGeneratorConfigurationYamlSwapper", keyGeneratorConfigurationYamlSwapper);
        when(keyGeneratorConfigurationYamlSwapper.swap(ArgumentMatchers.<KeyGeneratorConfiguration>any())).thenReturn(mock(YamlKeyGeneratorConfiguration.class));
        when(keyGeneratorConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlKeyGeneratorConfiguration>any())).thenReturn(mock(KeyGeneratorConfiguration.class));
        setSwapper("encryptorConfigurationYamlSwapper", encryptorConfigurationYamlSwapper);
        when(encryptorConfigurationYamlSwapper.swap(ArgumentMatchers.<EncryptorConfiguration>any())).thenReturn(mock(YamlEncryptorConfiguration.class));
        when(encryptorConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlEncryptorConfiguration>any())).thenReturn(mock(EncryptorConfiguration.class));
    }
    
    private void setSwapper(final String swapperFieldName, final YamlSwapper swapperFieldValue) throws ReflectiveOperationException {
        Field field = TableRuleConfigurationYamlSwapper.class.getDeclaredField(swapperFieldName);
        field.setAccessible(true);
        field.set(tableRuleConfigurationYamlSwapper, swapperFieldValue);
    }
    
    @Test
    public void assertSwapToYamlWithMinProperties() {
        YamlTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(new TableRuleConfiguration("tbl", "ds_$->{0..1}.tbl_$->{0..1}"));
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNull(actual.getDatabaseStrategy());
        assertNull(actual.getTableStrategy());
        assertNull(actual.getKeyGenerator());
        assertNull(actual.getEncryptor());
        assertNull(actual.getLogicIndex());
    }
    
    @Test
    public void assertSwapToYamlWithMaxProperties() {
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("tbl", "ds_$->{0..1}.tbl_$->{0..1}");
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(mock(InlineShardingStrategyConfiguration.class));
        tableRuleConfiguration.setTableShardingStrategyConfig(mock(InlineShardingStrategyConfiguration.class));
        tableRuleConfiguration.setKeyGeneratorConfig(mock(KeyGeneratorConfiguration.class));
        tableRuleConfiguration.setEncryptorConfig(mock(EncryptorConfiguration.class));
        tableRuleConfiguration.setLogicIndex("idx");
        YamlTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(tableRuleConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseStrategy());
        assertNotNull(actual.getTableStrategy());
        assertNotNull(actual.getKeyGenerator());
        assertNotNull(actual.getEncryptor());
        assertThat(actual.getLogicIndex(), is("idx"));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new TableRuleConfigurationYamlSwapper().swap(new YamlTableRuleConfiguration());
    }
    
    @Test
    public void assertSwapToObjectWithMinProperties() {
        YamlTableRuleConfiguration yamlConfiguration = new YamlTableRuleConfiguration();
        yamlConfiguration.setLogicTable("tbl");
        yamlConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        TableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNull(actual.getDatabaseShardingStrategyConfig());
        assertNull(actual.getTableShardingStrategyConfig());
        assertNull(actual.getKeyGeneratorConfig());
        assertNull(actual.getEncryptorConfig());
        assertNull(actual.getLogicIndex());
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlTableRuleConfiguration yamlConfiguration = new YamlTableRuleConfiguration();
        yamlConfiguration.setLogicTable("tbl");
        yamlConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        yamlConfiguration.setDatabaseStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setTableStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setKeyGenerator(mock(YamlKeyGeneratorConfiguration.class));
        yamlConfiguration.setEncryptor(mock(YamlEncryptorConfiguration.class));
        yamlConfiguration.setLogicIndex("idx");
        TableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseShardingStrategyConfig());
        assertNotNull(actual.getTableShardingStrategyConfig());
        assertNotNull(actual.getKeyGeneratorConfig());
        assertNotNull(actual.getEncryptorConfig());
        assertThat(actual.getLogicIndex(), is("idx"));
    }
}
