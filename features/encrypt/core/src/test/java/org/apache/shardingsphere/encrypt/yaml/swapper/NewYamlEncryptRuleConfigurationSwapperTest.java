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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NewYamlEncryptRuleConfigurationSwapperTest {
    
    private final NewYamlEncryptRuleConfigurationSwapper swapper = new NewYamlEncryptRuleConfigurationSwapper();
    
    @Test
    void assertSwapEmptyConfigToDataNodes() {
        EncryptRuleConfiguration config = new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigToDataNodes() {
        EncryptRuleConfiguration config = createMaximumEncryptRule();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(2));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("encryptors/FOO"));
        assertThat(iterator.next().getKey(), is("tables/foo"));
    }
    
    private EncryptRuleConfiguration createMaximumEncryptRule() {
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        tables.add(new EncryptTableRuleConfiguration("foo", Collections.singleton(new EncryptColumnRuleConfiguration("foo_column", new EncryptColumnItemRuleConfiguration("FIXTURE", "FOO")))));
        return new EncryptRuleConfiguration(tables, Collections.singletonMap("FOO", new AlgorithmConfiguration("FOO", new Properties())));
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        Collection<YamlDataNode> config = new LinkedList<>();
        assertFalse(swapper.swapToObject(config).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<YamlDataNode> config = new LinkedList<>();
        config.add(new YamlDataNode("/metadata/foo_db/rules/encrypt/tables/foo/versions/0", "columns:\n"
                + "  foo_column:\n"
                + "    cipher:\n"
                + "      encryptorName: FOO\n"
                + "      name: FIXTURE\n"
                + "    name: foo_column\n"
                + "name: foo\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/encrypt/encryptors/FOO/versions/0", "type: FOO\n"));
        EncryptRuleConfiguration result = swapper.swapToObject(config).get();
        assertThat(result.getTables().size(), is(1));
        assertThat(result.getTables().iterator().next().getName(), is("foo"));
        assertThat(result.getTables().iterator().next().getColumns().size(), is(1));
        assertThat(result.getTables().iterator().next().getColumns().iterator().next().getName(), is("foo_column"));
        assertThat(result.getTables().iterator().next().getColumns().iterator().next().getCipher().getName(), is("FIXTURE"));
        assertThat(result.getTables().iterator().next().getColumns().iterator().next().getCipher().getEncryptorName(), is("FOO"));
        assertThat(result.getEncryptors().size(), is(1));
        assertThat(result.getEncryptors().get("FOO").getType(), is("FOO"));
        assertThat(result.getEncryptors().get("FOO").getProps().size(), is(0));
    }
}
