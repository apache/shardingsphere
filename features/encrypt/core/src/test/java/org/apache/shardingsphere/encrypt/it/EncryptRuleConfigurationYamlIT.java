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

package org.apache.shardingsphere.encrypt.it;

import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EncryptRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    EncryptRuleConfigurationYamlIT() {
        super("yaml/encrypt-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertEncryptRule((YamlEncryptRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertEncryptRule(final YamlEncryptRuleConfiguration actual) {
        assertTables(actual.getTables());
        assertEncryptAlgorithm(actual.getEncryptors());
    }
    
    private void assertTables(final Map<String, YamlEncryptTableRuleConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("t_user").getColumns().size(), is(1));
        assertThat(actual.get("t_user").getColumns().get("username").getCipher().getName(), is("username_cipher"));
        assertThat(actual.get("t_user").getColumns().get("username").getCipher().getEncryptorName(), is("aes_encryptor"));
        assertThat(actual.get("t_user").getColumns().get("username").getAssistedQuery().getName(), is("assisted_query_username"));
        assertThat(actual.get("t_user").getColumns().get("username").getAssistedQuery().getEncryptorName(), is("assisted_encryptor"));
    }
    
    private void assertEncryptAlgorithm(final Map<String, YamlAlgorithmConfiguration> actual) {
        assertThat(actual.size(), is(2));
        assertThat(actual.get("aes_encryptor").getType(), is("AES"));
        assertThat(actual.get("aes_encryptor").getProps().getProperty("aes-key-value"), is("123456abc"));
        assertThat(actual.get("aes_encryptor").getProps().getProperty("digest-algorithm-name"), is("SHA-1"));
        assertThat(actual.get("assisted_encryptor").getType(), is("AES"));
        assertThat(actual.get("assisted_encryptor").getProps().getProperty("aes-key-value"), is("123456abc"));
        assertThat(actual.get("assisted_encryptor").getProps().getProperty("digest-algorithm-name"), is("SHA-1"));
    }
}
