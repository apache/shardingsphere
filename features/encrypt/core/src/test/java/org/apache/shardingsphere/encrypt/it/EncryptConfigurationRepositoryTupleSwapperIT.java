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
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EncryptConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    EncryptConfigurationRepositoryTupleSwapperIT() {
        super("yaml/encrypt-rule.yaml", new EncryptRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(3));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertEncryptors(actual.subList(0, 2), ((YamlEncryptRuleConfiguration) expectedYamlRuleConfig).getEncryptors());
        assertTables(actual.get(2), ((YamlEncryptRuleConfiguration) expectedYamlRuleConfig).getTables());
    }
    
    private void assertEncryptors(final List<RepositoryTuple> actual, final Map<String, YamlAlgorithmConfiguration> expectedEncryptors) {
        assertRepositoryTuple(actual.get(0), "encryptors/aes_encryptor", expectedEncryptors.get("aes_encryptor"));
        assertRepositoryTuple(actual.get(1), "encryptors/assisted_encryptor", expectedEncryptors.get("assisted_encryptor"));
    }
    
    private void assertTables(final RepositoryTuple actual, final Map<String, YamlEncryptTableRuleConfiguration> expectedTables) {
        assertRepositoryTuple(actual, "tables/t_user", expectedTables.get("t_user"));
    }
}
