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

package org.apache.shardingsphere.encrypt.yaml;

import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

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
        assertColumns(actual);
        assertEncryptAlgorithm(actual);
        assertLikeEncryptAlgorithm(actual);
    }
    
    private void assertColumns(final YamlEncryptRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().get("t_user").getColumns().size(), is(1));
        assertThat(actual.getTables().get("t_user").getColumns().get("username").getCipherColumn(), is("username_cipher"));
        assertThat(actual.getTables().get("t_user").getColumns().get("username").getEncryptorName(), is("username_encryptor"));
        assertThat(actual.getTables().get("t_user").getColumns().get("username").getAssistedQueryColumn(), is("assisted_query_username"));
        assertThat(actual.getTables().get("t_user").getColumns().get("username").getAssistedQueryEncryptorName(), is("assisted_encryptor"));
        assertThat(actual.getTables().get("t_user").getColumns().get("username").getLikeQueryColumn(), is("like_query_username"));
        assertThat(actual.getTables().get("t_user").getColumns().get("username").getLikeQueryEncryptorName(), is("like_encryptor"));
    }
    
    private void assertEncryptAlgorithm(final YamlEncryptRuleConfiguration actual) {
        assertThat(actual.getEncryptors().size(), is(2));
        assertThat(actual.getEncryptors().get("username_encryptor").getType(), is("AES"));
        assertThat(actual.getEncryptors().get("username_encryptor").getProps().get("aes-key-value"), is("123456abc"));
    }
    
    private void assertLikeEncryptAlgorithm(final YamlEncryptRuleConfiguration actual) {
        assertThat(actual.getLikeEncryptors().size(), is(1));
        assertThat(actual.getLikeEncryptors().get("like_encryptor").getType(), is("CHAR_DIGEST_LIKE"));
    }
}
