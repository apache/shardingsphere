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

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class EncryptColumnRuleConfigurationYamlSwapperTest {
    
    private static final String TEST_LOGIC_COLUMN = "encrypt_column";
    
    private static final String TEST_CIPHER_COLUMN = "encrypt_cipher";
    
    private static final String TEST_ASSISTED_QUERY_COLUMN = "encrypt_assisted";
    
    private static final String TEST_PLAIN_COLUMN = "encrypt_plain";
    
    private static final String TEST_ENCRYPTOR_NAME = "test_encryptor";
    
    private final EncryptColumnRuleConfigurationYamlSwapper columnYamlSwapper = new EncryptColumnRuleConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnConfig =
            new EncryptColumnRuleConfiguration(TEST_LOGIC_COLUMN, TEST_CIPHER_COLUMN, TEST_ASSISTED_QUERY_COLUMN, TEST_PLAIN_COLUMN, TEST_ENCRYPTOR_NAME);
        YamlEncryptColumnRuleConfiguration actualYamlEncryptColumnConfig = columnYamlSwapper.swapToYamlConfiguration(encryptColumnConfig);
        assertNotNull(actualYamlEncryptColumnConfig);
        assertThat(actualYamlEncryptColumnConfig.getCipherColumn(), is(TEST_CIPHER_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getAssistedQueryColumn(), is(TEST_ASSISTED_QUERY_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getPlainColumn(), is(TEST_PLAIN_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getEncryptorName(), is(TEST_ENCRYPTOR_NAME));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlEncryptColumnRuleConfiguration actualYamlEncryptColumnConfig = new YamlEncryptColumnRuleConfiguration();
        actualYamlEncryptColumnConfig.setLogicColumn(TEST_LOGIC_COLUMN);
        actualYamlEncryptColumnConfig.setCipherColumn(TEST_CIPHER_COLUMN);
        actualYamlEncryptColumnConfig.setAssistedQueryColumn(TEST_ASSISTED_QUERY_COLUMN);
        actualYamlEncryptColumnConfig.setPlainColumn(TEST_PLAIN_COLUMN);
        actualYamlEncryptColumnConfig.setEncryptorName(TEST_ENCRYPTOR_NAME);
        EncryptColumnRuleConfiguration actualEncryptColumnConfig = columnYamlSwapper.swapToObject(actualYamlEncryptColumnConfig);
        assertNotNull(actualEncryptColumnConfig);
        assertThat(actualEncryptColumnConfig.getLogicColumn(), is(TEST_LOGIC_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getCipherColumn(), is(TEST_CIPHER_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getAssistedQueryColumn(), is(TEST_ASSISTED_QUERY_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getPlainColumn(), is(TEST_PLAIN_COLUMN));
        assertThat(actualYamlEncryptColumnConfig.getEncryptorName(), is(TEST_ENCRYPTOR_NAME));
    }
}
