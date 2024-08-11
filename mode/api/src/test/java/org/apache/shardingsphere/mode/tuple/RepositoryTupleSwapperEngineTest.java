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

package org.apache.shardingsphere.mode.tuple;

import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.tuple.fixture.leaf.GlobalLeafYamlRuleConfiguration;
import org.apache.shardingsphere.mode.tuple.fixture.leaf.LeafYamlRuleConfiguration;
import org.apache.shardingsphere.mode.tuple.fixture.node.NodeYamlRuleConfiguration;
import org.apache.shardingsphere.mode.tuple.fixture.node.NodeYamlRuleConfigurationEnum;
import org.apache.shardingsphere.mode.tuple.fixture.none.NoneYamlRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        Collection<RepositoryTuple> actual = new RepositoryTupleSwapperEngine().swapToRepositoryTuples(new NodeYamlRuleConfiguration());
        assertTrue(actual.isEmpty());
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
        List<RepositoryTuple> actual = new ArrayList<>(new RepositoryTupleSwapperEngine().swapToRepositoryTuples(yamlRuleConfig));
        assertThat(actual.size(), is(9));
        assertThat(actual.get(0).getKey(), is("map_value/k"));
        assertThat(actual.get(0).getValue(), is("value: v" + System.lineSeparator()));
        assertThat(actual.get(1).getKey(), is("collection_value"));
        assertThat(actual.get(1).getValue(), is("- !!org.apache.shardingsphere.mode.tuple.fixture.leaf.LeafYamlRuleConfiguration" + System.lineSeparator() + "  value: foo" + System.lineSeparator()));
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
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithoutRepositoryTupleEntityAnnotation() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(Collections.emptyList(), NoneYamlRuleConfiguration.class).isPresent());
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithoutGlobalLeafYamlRuleConfiguration() {
        assertFalse(new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(
                Collections.singleton(new RepositoryTuple("/rules/invalid/versions/0", "value: foo" + System.lineSeparator())), GlobalLeafYamlRuleConfiguration.class).isPresent());
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithGlobalLeafYamlRuleConfiguration() {
        Optional<YamlRuleConfiguration> actual = new RepositoryTupleSwapperEngine().swapToYamlRuleConfiguration(
                Collections.singleton(new RepositoryTuple("/rules/leaf/versions/0", "value: foo" + System.lineSeparator())), GlobalLeafYamlRuleConfiguration.class);
        assertTrue(actual.isPresent());
        GlobalLeafYamlRuleConfiguration actualYamlConfig = (GlobalLeafYamlRuleConfiguration) actual.get();
        assertThat(actualYamlConfig.getValue(), is("foo"));
    }
}
