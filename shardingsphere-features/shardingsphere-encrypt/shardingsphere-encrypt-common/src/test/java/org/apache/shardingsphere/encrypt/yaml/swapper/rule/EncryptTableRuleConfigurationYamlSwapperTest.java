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

package org.apache.shardingsphere.encrypt.yaml.swapper.rule;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class EncryptTableRuleConfigurationYamlSwapperTest {
    
    private final EncryptTableRuleConfigurationYamlSwapper tableRuleConfigYamlSwapper = new EncryptTableRuleConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        Collection<EncryptColumnRuleConfiguration> encryptColumnRuleConfigCollection =
            Lists.newArrayList(
                new EncryptColumnRuleConfiguration("encrypt_column_1", "encrypt_cipher_1", "", "", "test_encryptor_1"),
                new EncryptColumnRuleConfiguration("encrypt_column_2", "encrypt_cipher_2", "", "", "test_encryptor_2"),
                new EncryptColumnRuleConfiguration("encrypt_column_3", "encrypt_cipher_3", "", "", "test_encryptor_3")
              );
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("test_table", encryptColumnRuleConfigCollection);
        YamlEncryptTableRuleConfiguration actualYamlEncryptTableRuleConfig = tableRuleConfigYamlSwapper.swapToYamlConfiguration(encryptTableRuleConfig);
        assertNotNull(actualYamlEncryptTableRuleConfig);
        Map<String, YamlEncryptColumnRuleConfiguration> actualColumns = actualYamlEncryptTableRuleConfig.getColumns();
        assertFalse(actualColumns.isEmpty());
        assertThat(actualColumns.size(), is(3));
        YamlEncryptColumnRuleConfiguration actualYamlEncryptColumnRuleConfigFirst = actualColumns.get("encrypt_column_1");
        assertThat(actualYamlEncryptColumnRuleConfigFirst.getCipherColumn(), is("encrypt_cipher_1"));
        assertThat(actualYamlEncryptColumnRuleConfigFirst.getEncryptorName(), is("test_encryptor_1"));
        YamlEncryptColumnRuleConfiguration actualYamlEncryptColumnRuleConfigSecond = actualColumns.get("encrypt_column_2");
        assertThat(actualYamlEncryptColumnRuleConfigSecond.getCipherColumn(), is("encrypt_cipher_2"));
        assertThat(actualYamlEncryptColumnRuleConfigSecond.getEncryptorName(), is("test_encryptor_2"));
        YamlEncryptColumnRuleConfiguration actualYamlEncryptColumnRuleConfigThird = actualColumns.get("encrypt_column_3");
        assertThat(actualYamlEncryptColumnRuleConfigThird.getCipherColumn(), is("encrypt_cipher_3"));
        assertThat(actualYamlEncryptColumnRuleConfigThird.getEncryptorName(), is("test_encryptor_3"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlEncryptColumnRuleConfiguration encryptColumnRuleConfig = new YamlEncryptColumnRuleConfiguration();
        encryptColumnRuleConfig.setLogicColumn("encrypt_column");
        encryptColumnRuleConfig.setCipherColumn("encrypt_cipher");
        encryptColumnRuleConfig.setAssistedQueryColumn("encrypt_assisted");
        encryptColumnRuleConfig.setPlainColumn("encrypt_plain");
        encryptColumnRuleConfig.setEncryptorName("test_encryptor");
        Map<String, YamlEncryptColumnRuleConfiguration> columns = new LinkedHashMap<>(1);
        columns.put("test_column", encryptColumnRuleConfig);
        YamlEncryptTableRuleConfiguration yamlEncryptTableRuleConfig = new YamlEncryptTableRuleConfiguration();
        yamlEncryptTableRuleConfig.setName("test_table");
        yamlEncryptTableRuleConfig.setColumns(columns);
        EncryptTableRuleConfiguration actualEncryptTableRuleConfig = tableRuleConfigYamlSwapper.swapToObject(yamlEncryptTableRuleConfig);
        assertNotNull(actualEncryptTableRuleConfig);
        assertThat(actualEncryptTableRuleConfig.getName(), is("test_table"));
        Collection<EncryptColumnRuleConfiguration> actualColumns = actualEncryptTableRuleConfig.getColumns();
        assertFalse(actualColumns.isEmpty());
        assertThat(actualColumns.size(), is(1));
    }
}
