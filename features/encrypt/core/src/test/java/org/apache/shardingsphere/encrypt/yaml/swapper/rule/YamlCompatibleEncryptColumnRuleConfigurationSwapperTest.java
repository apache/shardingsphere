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

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlCompatibleEncryptColumnRuleConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Deprecated
class YamlCompatibleEncryptColumnRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlCompatibleEncryptColumnRuleConfigurationSwapper swapper = new YamlCompatibleEncryptColumnRuleConfigurationSwapper();
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("logicColumn", new EncryptColumnItemRuleConfiguration("cipherColumn", "encryptorName"));
        encryptColumnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("assistedQueryColumn", "foo_assist_query_encryptor"));
        encryptColumnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("likeQueryColumn", "foo_like_encryptor"));
        YamlCompatibleEncryptColumnRuleConfiguration actual = swapper.swapToYamlConfiguration(encryptColumnRuleConfig);
        assertThat(actual.getLogicColumn(), is("logicColumn"));
        assertThat(actual.getCipherColumn(), is("cipherColumn"));
        assertThat(actual.getAssistedQueryColumn(), is("assistedQueryColumn"));
        assertThat(actual.getLikeQueryColumn(), is("likeQueryColumn"));
        assertThat(actual.getEncryptorName(), is("encryptorName"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlCompatibleEncryptColumnRuleConfigurationSwapper swapper = new YamlCompatibleEncryptColumnRuleConfigurationSwapper();
        YamlCompatibleEncryptColumnRuleConfiguration yamlEncryptColumnRuleConfig = new YamlCompatibleEncryptColumnRuleConfiguration();
        yamlEncryptColumnRuleConfig.setLogicColumn("logicColumn");
        yamlEncryptColumnRuleConfig.setCipherColumn("cipherColumn");
        yamlEncryptColumnRuleConfig.setAssistedQueryColumn("assistedQueryColumn");
        yamlEncryptColumnRuleConfig.setLikeQueryColumn("likeQueryColumn");
        yamlEncryptColumnRuleConfig.setEncryptorName("encryptorName");
        EncryptColumnRuleConfiguration actual = swapper.swapToObject(yamlEncryptColumnRuleConfig);
        assertThat(actual.getName(), is("logicColumn"));
        assertThat(actual.getCipher().getName(), is("cipherColumn"));
        assertTrue(actual.getAssistedQuery().isPresent());
        assertThat(actual.getAssistedQuery().get().getName(), is("assistedQueryColumn"));
        assertTrue(actual.getLikeQuery().isPresent());
        assertThat(actual.getLikeQuery().get().getName(), is("likeQueryColumn"));
        assertThat(actual.getCipher().getEncryptorName(), is("encryptorName"));
    }
}
