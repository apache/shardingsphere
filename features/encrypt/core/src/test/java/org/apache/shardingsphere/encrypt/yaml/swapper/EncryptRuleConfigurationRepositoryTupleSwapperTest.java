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

package org.apache.shardingsphere.encrypt.yaml.swapper;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleConfigurationRepositoryTupleSwapperTest {
    
    private final EncryptRuleConfigurationRepositoryTupleSwapper swapper = new EncryptRuleConfigurationRepositoryTupleSwapper();
    
    @Test
    void assertSwapToRepositoryTuplesWithEmptyRule() {
        assertTrue(swapper.swapToRepositoryTuples(new YamlEncryptRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToRepositoryTuples() {
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples(
                (YamlEncryptRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(createMaximumEncryptRule()));
        assertThat(actual.size(), is(2));
        Iterator<RepositoryTuple> iterator = actual.iterator();
        assertThat(iterator.next().getKey(), is("encryptors/FOO"));
        assertThat(iterator.next().getKey(), is("tables/foo"));
    }
    
    private EncryptRuleConfiguration createMaximumEncryptRule() {
        Collection<EncryptTableRuleConfiguration> tables = Collections.singleton(
                new EncryptTableRuleConfiguration("foo", Collections.singleton(new EncryptColumnRuleConfiguration("foo_column", new EncryptColumnItemRuleConfiguration("FIXTURE", "FOO")))));
        return new EncryptRuleConfiguration(tables, Collections.singletonMap("FOO", new AlgorithmConfiguration("FOO", new Properties())));
    }
    
    @Test
    void assertSwapToObjectWithEmptyTuple() {
        assertFalse(swapper.swapToObject(Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<RepositoryTuple> repositoryTuples = Arrays.asList(new RepositoryTuple("/metadata/foo_db/rules/encrypt/tables/foo/versions/0", "columns:\n"
                + "  foo_column:\n"
                + "    cipher:\n"
                + "      encryptorName: FOO\n"
                + "      name: FIXTURE\n"
                + "    name: foo_column\n"
                + "name: foo\n"), new RepositoryTuple("/metadata/foo_db/rules/encrypt/encryptors/FOO/versions/0", "type: FOO\n"));
        Optional<YamlEncryptRuleConfiguration> actual = swapper.swapToObject(repositoryTuples);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTables().size(), is(1));
        assertThat(actual.get().getTables().get("foo").getColumns().size(), is(1));
        assertThat(actual.get().getTables().get("foo").getColumns().get("foo_column").getName(), is("foo_column"));
        assertThat(actual.get().getTables().get("foo").getColumns().get("foo_column").getCipher().getName(), is("FIXTURE"));
        assertThat(actual.get().getTables().get("foo").getColumns().get("foo_column").getCipher().getEncryptorName(), is("FOO"));
        assertThat(actual.get().getEncryptors().size(), is(1));
        assertThat(actual.get().getEncryptors().get("FOO").getType(), is("FOO"));
        assertTrue(actual.get().getEncryptors().get("FOO").getProps().isEmpty());
    }
}
