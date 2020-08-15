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
import static org.junit.Assert.assertThat;

public final class EncryptColumnRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        EncryptColumnRuleConfigurationYamlSwapper encryptColumnRuleConfigurationYamlSwapper = new EncryptColumnRuleConfigurationYamlSwapper();
        EncryptColumnRuleConfiguration encryptColumnRuleConfiguration = new EncryptColumnRuleConfiguration("logicColumn", "cipherColumn", "assistedQueryColumn", "plainColumn", "encryptorName");
        YamlEncryptColumnRuleConfiguration actual = encryptColumnRuleConfigurationYamlSwapper.swapToYamlConfiguration(encryptColumnRuleConfiguration);
        assertThat(actual.getCipherColumn(), is("cipherColumn"));
        assertThat(actual.getAssistedQueryColumn(), is("assistedQueryColumn"));
        assertThat(actual.getPlainColumn(), is("plainColumn"));
        assertThat(actual.getEncryptorName(), is("encryptorName"));
    }
    
    @Test
    public void assertSwapToObject() {
        EncryptColumnRuleConfigurationYamlSwapper encryptColumnRuleConfigurationYamlSwapper = new EncryptColumnRuleConfigurationYamlSwapper();
        YamlEncryptColumnRuleConfiguration yamlEncryptColumnRuleConfiguration = new YamlEncryptColumnRuleConfiguration();
        yamlEncryptColumnRuleConfiguration.setLogicColumn("logicColumn");
        yamlEncryptColumnRuleConfiguration.setCipherColumn("cipherColumn");
        yamlEncryptColumnRuleConfiguration.setAssistedQueryColumn("assistedQueryColumn");
        yamlEncryptColumnRuleConfiguration.setPlainColumn("plainColumn");
        yamlEncryptColumnRuleConfiguration.setEncryptorName("encryptorName");
        EncryptColumnRuleConfiguration actual = encryptColumnRuleConfigurationYamlSwapper.swapToObject(yamlEncryptColumnRuleConfiguration);
        assertThat(actual.getLogicColumn(), is("logicColumn"));
        assertThat(actual.getCipherColumn(), is("cipherColumn"));
        assertThat(actual.getAssistedQueryColumn(), is("assistedQueryColumn"));
        assertThat(actual.getPlainColumn(), is("plainColumn"));
        assertThat(actual.getEncryptorName(), is("encryptorName"));
    }
}
