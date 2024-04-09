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
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlEncryptColumnRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlEncryptColumnRuleConfigurationSwapper swapper = new YamlEncryptColumnRuleConfigurationSwapper();
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("logicColumn", new EncryptColumnItemRuleConfiguration("cipherColumn", "encryptorName"));
        encryptColumnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("assistedQueryColumn", "foo_assist_query_encryptor"));
        encryptColumnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("likeQueryColumn", "foo_like_encryptor"));
        YamlEncryptColumnRuleConfiguration actual = swapper.swapToYamlConfiguration(encryptColumnRuleConfig);
        assertThat(actual.getName(), is("logicColumn"));
        assertThat(actual.getCipher().getName(), is("cipherColumn"));
        assertThat(actual.getCipher().getEncryptorName(), is("encryptorName"));
        assertThat(actual.getAssistedQuery().getName(), is("assistedQueryColumn"));
        assertThat(actual.getAssistedQuery().getEncryptorName(), is("foo_assist_query_encryptor"));
        assertThat(actual.getLikeQuery().getName(), is("likeQueryColumn"));
        assertThat(actual.getLikeQuery().getEncryptorName(), is("foo_like_encryptor"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlEncryptColumnRuleConfigurationSwapper swapper = new YamlEncryptColumnRuleConfigurationSwapper();
        EncryptColumnRuleConfiguration actual = swapper.swapToObject(buildYamlEncryptColumnRuleConfiguration());
        assertThat(actual.getName(), is("logicColumn"));
        assertThat(actual.getCipher().getName(), is("cipherColumn"));
        assertTrue(actual.getAssistedQuery().isPresent());
        assertThat(actual.getAssistedQuery().get().getName(), is("assistedQueryColumn"));
        assertTrue(actual.getLikeQuery().isPresent());
        assertThat(actual.getLikeQuery().get().getName(), is("likeQueryColumn"));
        assertThat(actual.getCipher().getEncryptorName(), is("encryptorName"));
    }
    
    private YamlEncryptColumnRuleConfiguration buildYamlEncryptColumnRuleConfiguration() {
        YamlEncryptColumnRuleConfiguration result = new YamlEncryptColumnRuleConfiguration();
        result.setName("logicColumn");
        YamlEncryptColumnItemRuleConfiguration cipherColumnConfig = new YamlEncryptColumnItemRuleConfiguration();
        cipherColumnConfig.setName("cipherColumn");
        cipherColumnConfig.setEncryptorName("encryptorName");
        result.setCipher(cipherColumnConfig);
        YamlEncryptColumnItemRuleConfiguration assistedQueryColumnConfig = new YamlEncryptColumnItemRuleConfiguration();
        assistedQueryColumnConfig.setName("assistedQueryColumn");
        result.setAssistedQuery(assistedQueryColumnConfig);
        YamlEncryptColumnItemRuleConfiguration likeQueryColumnConfig = new YamlEncryptColumnItemRuleConfiguration();
        likeQueryColumnConfig.setName("likeQueryColumn");
        result.setLikeQuery(likeQueryColumnConfig);
        return result;
    }
}
