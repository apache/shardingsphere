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

import org.apache.shardingsphere.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlEncryptorConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlKeyGeneratorConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlInlineShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class TableRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlWithMinProperties() {
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration();
        tableRuleConfiguration.setLogicTable("tbl");
        tableRuleConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        YamlTableRuleConfiguration actual = new TableRuleConfigurationYamlSwapper().swap(tableRuleConfiguration);
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
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration();
        tableRuleConfiguration.setLogicTable("tbl");
        tableRuleConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "ds_$->{id%2}"));
        tableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "tbl_$->{id%2}"));
        tableRuleConfiguration.setKeyGeneratorConfig(new KeyGeneratorConfiguration("id", "UUID", new Properties()));
        tableRuleConfiguration.setEncryptorConfig(new EncryptorConfiguration("MD5", "pwd", new Properties()));
        tableRuleConfiguration.setLogicIndex("idx");
        YamlTableRuleConfiguration actual = new TableRuleConfigurationYamlSwapper().swap(tableRuleConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertThat(actual.getDatabaseStrategy().getInline().getShardingColumn(), is("id"));
        assertThat(actual.getDatabaseStrategy().getInline().getAlgorithmExpression(), is("ds_$->{id%2}"));
        assertThat(actual.getTableStrategy().getInline().getShardingColumn(), is("id"));
        assertThat(actual.getTableStrategy().getInline().getAlgorithmExpression(), is("tbl_$->{id%2}"));
        assertThat(actual.getKeyGenerator().getColumn(), is("id"));
        assertThat(actual.getKeyGenerator().getType(), is("UUID"));
        assertThat(actual.getKeyGenerator().getProps(), is(new Properties()));
        assertThat(actual.getEncryptor().getType(), is("MD5"));
        assertThat(actual.getEncryptor().getColumns(), is("pwd"));
        assertThat(actual.getEncryptor().getProps(), is(new Properties()));
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
        TableRuleConfiguration actual = new TableRuleConfigurationYamlSwapper().swap(yamlConfiguration);
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
        yamlConfiguration.setDatabaseStrategy(createYamlDatabaseStrategyConfiguration());
        yamlConfiguration.setTableStrategy(createYamlTableStrategyConfiguration());
        yamlConfiguration.setKeyGenerator(createYamlKeyGeneratorConfiguration());
        yamlConfiguration.setEncryptor(createYamlEncryptorConfiguration());
        yamlConfiguration.setLogicIndex("idx");
        TableRuleConfiguration actual = new TableRuleConfigurationYamlSwapper().swap(yamlConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertThat(((InlineShardingStrategyConfiguration) actual.getDatabaseShardingStrategyConfig()).getShardingColumn(), is("id"));
        assertThat(((InlineShardingStrategyConfiguration) actual.getDatabaseShardingStrategyConfig()).getAlgorithmExpression(), is("ds_$->{id%2}"));
        assertThat(((InlineShardingStrategyConfiguration) actual.getTableShardingStrategyConfig()).getShardingColumn(), is("id"));
        assertThat(((InlineShardingStrategyConfiguration) actual.getTableShardingStrategyConfig()).getAlgorithmExpression(), is("tbl_$->{id%2}"));
        assertThat(actual.getKeyGeneratorConfig().getColumn(), is("id"));
        assertThat(actual.getKeyGeneratorConfig().getType(), is("UUID"));
        assertThat(actual.getKeyGeneratorConfig().getProps(), is(new Properties()));
        assertThat(actual.getEncryptorConfig().getType(), is("MD5"));
        assertThat(actual.getEncryptorConfig().getColumns(), is("pwd"));
        assertThat(actual.getEncryptorConfig().getProps(), is(new Properties()));
        assertThat(actual.getLogicIndex(), is("idx"));
    }
    
    private YamlShardingStrategyConfiguration createYamlDatabaseStrategyConfiguration() {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlInlineShardingStrategyConfiguration inlineStrategyConfiguration = new YamlInlineShardingStrategyConfiguration();
        inlineStrategyConfiguration.setShardingColumn("id");
        inlineStrategyConfiguration.setAlgorithmExpression("ds_$->{id%2}");
        result.setInline(inlineStrategyConfiguration);
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYamlTableStrategyConfiguration() {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlInlineShardingStrategyConfiguration inlineStrategyConfiguration = new YamlInlineShardingStrategyConfiguration();
        inlineStrategyConfiguration.setShardingColumn("id");
        inlineStrategyConfiguration.setAlgorithmExpression("tbl_$->{id%2}");
        result.setInline(inlineStrategyConfiguration);
        return result;
    }
    
    private YamlKeyGeneratorConfiguration createYamlKeyGeneratorConfiguration() {
        YamlKeyGeneratorConfiguration result = new YamlKeyGeneratorConfiguration();
        result.setType("UUID");
        result.setColumn("id");
        return result;
    }
    
    private YamlEncryptorConfiguration createYamlEncryptorConfiguration() {
        YamlEncryptorConfiguration result = new YamlEncryptorConfiguration();
        result.setType("MD5");
        result.setColumns("pwd");
        return result;
    }
}
