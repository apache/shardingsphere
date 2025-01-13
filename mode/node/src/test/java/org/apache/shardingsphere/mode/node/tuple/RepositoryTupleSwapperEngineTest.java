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

package org.apache.shardingsphere.mode.node.tuple;

import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.tuple.fixture.leaf.GlobalLeafYamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.tuple.fixture.leaf.LeafYamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.tuple.fixture.node.NodeYamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.tuple.fixture.node.NodeYamlRuleConfigurationEnum;
import org.apache.shardingsphere.mode.node.tuple.fixture.none.NoneYamlRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTupleSwapperEngineTest {
    
    @Test
    void assertSwapToRepositoryTuplesWithoutRepositoryTupleEntityAnnotation() {
        assertTrue(new RepositoryTupleSwapperEngine().swapToRepositoryTuples(new NoneYamlRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToRepositoryTuplesWithLeafYamlRuleConfiguration() {
        Collection<RepositoryTuple> actual = new RepositoryTupleSwapperEngine().swapToRepositoryTuples(new LeafYamlRuleConfiguration("foo"));
        assertThat(actual.size(), is(1));
        RepositoryTuple actualTuple = actual.iterator().next();
        assertThat(actualTuple.getKey(), is("leaf"));
        assertThat(actualTuple.getValue(), is("value: foo" + System.lineSeparator()));
    }
    
    @Test
    void assertSwapToRepositoryTuplesWithEmptyNodeYamlRuleConfiguration() {
        assertTrue(new RepositoryTupleSwapperEngine().swapToRepositoryTuples(new NodeYamlRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToRepositoryTuplesWithNodeYamlRuleConfiguration() {
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
        List<RepositoryTuple> actual = new ArrayList<>(new RepositoryTupleSwapperEngine().swapToRepositoryTuples(yamlRuleConfig));
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
    void assertSwapToYamlRuleConfigurationWithoutRepositoryTupleEntityAnnotation() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(Collections.emptyList(), NoneYamlRuleConfiguration.class).isPresent());
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithoutGlobalLeafYamlRuleConfiguration() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(Collections.singleton(new RepositoryTuple("invalid", "")), GlobalLeafYamlRuleConfiguration.class).isPresent());
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithGlobalLeafYamlRuleConfiguration() {
        Optional<YamlRuleConfiguration> actual = new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(
                Collections.singleton(new RepositoryTuple("/rules/leaf/versions/0", "value: foo")), GlobalLeafYamlRuleConfiguration.class);
        assertTrue(actual.isPresent());
        GlobalLeafYamlRuleConfiguration actualYamlConfig = (GlobalLeafYamlRuleConfiguration) actual.get();
        assertThat(actualYamlConfig.getValue(), is("foo"));
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithInvalidLeafYamlRuleConfiguration() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(Collections.singleton(new RepositoryTuple("/invalid", "foo")), LeafYamlRuleConfiguration.class).isPresent());
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithInvalidNodeYamlRuleConfiguration() {
        Optional<YamlRuleConfiguration> actual = new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(
                Collections.singleton(new RepositoryTuple("/invalid", "foo")), NodeYamlRuleConfiguration.class);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithEmptyNodeYamlRuleConfiguration() {
        Optional<YamlRuleConfiguration> actual = new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(
                Collections.singleton(new RepositoryTuple("/metadata/foo_db/rules/node/string_value/versions/0", "")), NodeYamlRuleConfiguration.class);
        assertTrue(actual.isPresent());
        NodeYamlRuleConfiguration actualYamlConfig = (NodeYamlRuleConfiguration) actual.get();
        assertThat(actualYamlConfig.getStringValue(), is(""));
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithNodeYamlRuleConfiguration() {
        Optional<YamlRuleConfiguration> actual = new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(Arrays.asList(
                new RepositoryTuple("/metadata/foo_db/rules/node/map_value/k/versions/0", "v"),
                new RepositoryTuple("/metadata/foo_db/rules/node/collection_value/versions/0", "- !LEAF" + System.lineSeparator() + "  value: foo"),
                new RepositoryTuple("/metadata/foo_db/rules/node/string_value/versions/0", "str"),
                new RepositoryTuple("/metadata/foo_db/rules/node/boolean_value/versions/0", "true"),
                new RepositoryTuple("/metadata/foo_db/rules/node/integer_value/versions/0", "1"),
                new RepositoryTuple("/metadata/foo_db/rules/node/long_value/versions/0", "10"),
                new RepositoryTuple("/metadata/foo_db/rules/node/enum_value/versions/0", "FOO")), NodeYamlRuleConfiguration.class);
        assertTrue(actual.isPresent());
        NodeYamlRuleConfiguration actualYamlConfig = (NodeYamlRuleConfiguration) actual.get();
        assertThat(actualYamlConfig.getMapValue().size(), is(1));
        assertThat(actualYamlConfig.getMapValue().get("k").getValue(), is("v"));
        assertThat(actualYamlConfig.getCollectionValue().size(), is(1));
        assertThat(actualYamlConfig.getCollectionValue().iterator().next().getValue(), is("foo"));
        assertThat(actualYamlConfig.getStringValue(), is("str"));
        assertTrue(actualYamlConfig.getBooleanValue());
        assertThat(actualYamlConfig.getIntegerValue(), is(1));
        assertThat(actualYamlConfig.getLongValue(), is(10L));
        assertThat(actualYamlConfig.getEnumValue(), is(NodeYamlRuleConfigurationEnum.FOO));
    }
    
    @Test
    void assertSwapToEmptyRuleConfigurations() {
        assertTrue(new RepositoryTupleSwapperEngine().swapToRuleConfigurations(Collections.emptyList()).isEmpty());
    }
    
    @Test
    void assertSwapToRuleConfigurations() {
        assertTrue(new RepositoryTupleSwapperEngine().swapToRuleConfigurations(Collections.singleton(new RepositoryTuple("/rules/leaf/versions/0", "value: foo"))).isEmpty());
    }
    
    @Test
    void assertSwapToEmptyRuleConfiguration() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToRuleConfiguration("leaf", Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertSwapToNotFoundRuleConfiguration() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToRuleConfiguration("invalid", Collections.singleton(new RepositoryTuple("/rules/leaf/versions/0", "value: foo"))).isPresent());
    }
    
    @Test
    void assertSwapToRuleConfiguration() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToRuleConfiguration("leaf", Collections.singleton(new RepositoryTuple("/rules/leaf/versions/0", "value: foo"))).isPresent());
    }
}
