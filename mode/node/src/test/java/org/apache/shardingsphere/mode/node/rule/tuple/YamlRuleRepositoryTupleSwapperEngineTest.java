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

package org.apache.shardingsphere.mode.node.rule.tuple;

import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.leaf.LeafYamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.node.NodeYamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.node.NodeYamlRuleConfigurationEnum;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.none.NoneYamlRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRuleRepositoryTupleSwapperEngineTest {
    
    @Test
    void assertSwapToTuplesWithoutTupleEntityAnnotation() {
        assertTrue(new YamlRuleRepositoryTupleSwapperEngine().swapToTuples(new NoneYamlRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToTuplesWithLeafYamlRuleConfiguration() {
        Collection<RuleRepositoryTuple> actual = new YamlRuleRepositoryTupleSwapperEngine().swapToTuples(new LeafYamlRuleConfiguration("foo"));
        assertThat(actual.size(), is(1));
        RuleRepositoryTuple actualTuple = actual.iterator().next();
        assertThat(actualTuple.getKey(), is("leaf"));
        assertThat(actualTuple.getValue(), is("value: foo" + System.lineSeparator()));
    }
    
    @Test
    void assertSwapToTuplesWithEmptyNodeYamlRuleConfiguration() {
        assertTrue(new YamlRuleRepositoryTupleSwapperEngine().swapToTuples(new NodeYamlRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToTuplesWithNodeYamlRuleConfiguration() {
        NodeYamlRuleConfiguration yamlRuleConfig = new NodeYamlRuleConfiguration();
        yamlRuleConfig.setMapValue(Collections.singletonMap("k", new LeafYamlRuleConfiguration("v")));
        yamlRuleConfig.setCollectionValue(Collections.singletonList(new LeafYamlRuleConfiguration("foo")));
        yamlRuleConfig.setStringValue("str");
        yamlRuleConfig.setBooleanValue(true);
        yamlRuleConfig.setIntegerValue(1);
        yamlRuleConfig.setLongValue(10L);
        yamlRuleConfig.setEnumValue(NodeYamlRuleConfigurationEnum.FOO);
        LeafYamlRuleConfiguration leaf = new LeafYamlRuleConfiguration();
        leaf.setValue("leaf");
        yamlRuleConfig.setLeaf(leaf);
        yamlRuleConfig.setGens(Collections.singleton("value"));
        yamlRuleConfig.setGen("single_gen");
        List<RuleRepositoryTuple> actual = new ArrayList<>(new YamlRuleRepositoryTupleSwapperEngine().swapToTuples(yamlRuleConfig));
        assertThat(actual.size(), is(10));
        assertThat(actual.get(0).getKey(), is("map_value/k"));
        assertThat(actual.get(0).getValue(), is("value: v" + System.lineSeparator()));
        assertThat(actual.get(1).getKey(), is("collection_value"));
        assertThat(actual.get(1).getValue(), is("- !LEAF" + System.lineSeparator() + "  value: foo" + System.lineSeparator()));
        assertThat(actual.get(2).getKey(), is("string_value"));
        assertThat(actual.get(2).getValue(), is("str"));
        assertThat(actual.get(3).getKey(), is("boolean_value"));
        assertThat(actual.get(3).getValue(), is("true"));
        assertThat(actual.get(4).getKey(), is("integer_value"));
        assertThat(actual.get(4).getValue(), is("1"));
        assertThat(actual.get(5).getKey(), is("long_value"));
        assertThat(actual.get(5).getValue(), is("10"));
        assertThat(actual.get(6).getKey(), is("enum_value"));
        assertThat(actual.get(6).getValue(), is("FOO"));
        assertThat(actual.get(7).getKey(), is("leaf"));
        assertThat(actual.get(7).getValue(), is("value: leaf" + System.lineSeparator()));
        assertThat(actual.get(8).getKey(), is("gens/gen: value"));
        assertThat(actual.get(8).getValue(), is("value"));
        assertThat(actual.get(9).getKey(), is("gen"));
        assertThat(actual.get(9).getValue(), is("single_gen"));
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithEmptyNodeYamlRuleConfiguration() {
        NodeYamlRuleConfiguration actual = (NodeYamlRuleConfiguration) new YamlRuleRepositoryTupleSwapperEngine().swapToYamlDatabaseRuleConfiguration("node",
                Collections.singleton(new RuleRepositoryTuple("/metadata/foo_db/rules/node/string_value", "")));
        assertThat(actual.getStringValue(), is(""));
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithNodeYamlRuleConfiguration() {
        NodeYamlRuleConfiguration actual = (NodeYamlRuleConfiguration) new YamlRuleRepositoryTupleSwapperEngine().swapToYamlDatabaseRuleConfiguration("node", Arrays.asList(
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/map_value/k", "v"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/map_value/k:qualified", "k:qualified"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/collection_value", "- !LEAF" + System.lineSeparator() + "  value: foo"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/string_value", "str"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/boolean_value", "true"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/integer_value", "1"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/long_value", "10"),
                new RuleRepositoryTuple("/metadata/foo_db/rules/node/enum_value", "FOO")));
        assertThat(actual.getMapValue().size(), is(2));
        assertThat(actual.getMapValue().get("k").getValue(), is("v"));
        assertThat(actual.getMapValue().get("k:qualified").getValue(), is("k:qualified"));
        assertThat(actual.getCollectionValue().size(), is(1));
        assertThat(actual.getCollectionValue().iterator().next().getValue(), is("foo"));
        assertThat(actual.getStringValue(), is("str"));
        assertTrue(actual.getBooleanValue());
        assertThat(actual.getIntegerValue(), is(1));
        assertThat(actual.getLongValue(), is(10L));
        assertThat(actual.getEnumValue(), is(NodeYamlRuleConfigurationEnum.FOO));
    }
    
    @Test
    void assertSwapToNotFoundYamlGlobalRuleConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new YamlRuleRepositoryTupleSwapperEngine().swapToYamlGlobalRuleConfiguration("invalid", "value: foo"));
    }
    
    @Test
    void assertSwapToYamlGlobalRuleConfiguration() {
        YamlRuleConfiguration actual = new YamlRuleRepositoryTupleSwapperEngine().swapToYamlGlobalRuleConfiguration("leaf", "value: foo");
        assertThat(((LeafYamlRuleConfiguration) actual).getValue(), is("foo"));
    }
}
