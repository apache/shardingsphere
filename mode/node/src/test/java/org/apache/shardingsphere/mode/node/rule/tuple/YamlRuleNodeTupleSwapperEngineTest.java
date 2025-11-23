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
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.leaf.YamlLeafRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.node.YamlNodeRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.node.YamlNodeRuleConfigurationEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRuleNodeTupleSwapperEngineTest {
    
    @Test
    void assertSwapToTuple() {
        RuleNodeTuple actual = new YamlRuleNodeTupleSwapperEngine().swapToTuple(new YamlLeafRuleConfiguration("foo"));
        assertThat(actual.getPath(), is("/rules/leaf"));
        assertThat(actual.getContent(), is("value: foo" + System.lineSeparator()));
    }
    
    @Test
    void assertSwapToTuplesWithEmptyNodeYamlRuleConfiguration() {
        assertTrue(new YamlRuleNodeTupleSwapperEngine().swapToTuples("foo_db", new YamlNodeRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToTuplesWithNodeYamlRuleConfiguration() {
        List<RuleNodeTuple> actual = new ArrayList<>(new YamlRuleNodeTupleSwapperEngine().swapToTuples("foo_db", createYamlNodeRuleConfiguration()));
        assertThat(actual.size(), is(10));
        assertThat(actual.get(0).getPath(), is("/metadata/foo_db/rules/node/map_value/k"));
        assertThat(actual.get(0).getContent(), is("value: v" + System.lineSeparator()));
        assertThat(actual.get(1).getPath(), is("/metadata/foo_db/rules/node/collection_value"));
        assertThat(actual.get(1).getContent(), is("- !LEAF" + System.lineSeparator() + "  value: foo" + System.lineSeparator()));
        assertThat(actual.get(2).getPath(), is("/metadata/foo_db/rules/node/string_value"));
        assertThat(actual.get(2).getContent(), is("str"));
        assertThat(actual.get(3).getPath(), is("/metadata/foo_db/rules/node/boolean_value"));
        assertThat(actual.get(3).getContent(), is("true"));
        assertThat(actual.get(4).getPath(), is("/metadata/foo_db/rules/node/integer_value"));
        assertThat(actual.get(4).getContent(), is("1"));
        assertThat(actual.get(5).getPath(), is("/metadata/foo_db/rules/node/long_value"));
        assertThat(actual.get(5).getContent(), is("10"));
        assertThat(actual.get(6).getPath(), is("/metadata/foo_db/rules/node/enum_value"));
        assertThat(actual.get(6).getContent(), is("FOO"));
        assertThat(actual.get(7).getPath(), is("/metadata/foo_db/rules/node/leaf"));
        assertThat(actual.get(7).getContent(), is("value: leaf" + System.lineSeparator()));
        assertThat(actual.get(8).getPath(), is("/metadata/foo_db/rules/node/gens/gen: value"));
        assertThat(actual.get(8).getContent(), is("value"));
        assertThat(actual.get(9).getPath(), is("/metadata/foo_db/rules/node/gen"));
        assertThat(actual.get(9).getContent(), is("single_gen"));
    }
    
    private YamlNodeRuleConfiguration createYamlNodeRuleConfiguration() {
        YamlNodeRuleConfiguration result = new YamlNodeRuleConfiguration();
        result.setMapValue(Collections.singletonMap("k", new YamlLeafRuleConfiguration("v")));
        result.setCollectionValue(Collections.singletonList(new YamlLeafRuleConfiguration("foo")));
        result.setStringValue("str");
        result.setBooleanValue(true);
        result.setIntegerValue(1);
        result.setLongValue(10L);
        result.setEnumValue(YamlNodeRuleConfigurationEnum.FOO);
        YamlLeafRuleConfiguration leaf = new YamlLeafRuleConfiguration();
        leaf.setValue("leaf");
        result.setLeaf(leaf);
        result.setGens(Collections.singleton("value"));
        result.setGen("single_gen");
        return result;
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithEmptyNodeYamlRuleConfiguration() {
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("string_value"));
        YamlNodeRuleConfiguration actual = (YamlNodeRuleConfiguration) new YamlRuleNodeTupleSwapperEngine().swapToYamlDatabaseRuleConfiguration("foo_db", "node",
                Collections.singleton(new RuleNodeTuple(databaseRuleNodePath, "")));
        assertThat(actual.getStringValue(), is(""));
    }
    
    @Test
    void assertSwapToYamlRuleConfigurationWithNodeYamlRuleConfiguration() {
        YamlNodeRuleConfiguration actual = (YamlNodeRuleConfiguration) new YamlRuleNodeTupleSwapperEngine().swapToYamlDatabaseRuleConfiguration("foo_db", "node", Arrays.asList(
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("map_value/k")), "v"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("map_value/k:qualified")), "k:qualified"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("collection_value")), "- !LEAF" + System.lineSeparator() + "  value: foo"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("string_value")), "str"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("boolean_value")), "true"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("integer_value")), "1"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("long_value")), "10"),
                new RuleNodeTuple(new DatabaseRuleNodePath("foo_db", "node", new DatabaseRuleItem("enum_value")), "FOO")));
        assertThat(actual.getMapValue().size(), is(2));
        assertThat(actual.getMapValue().get("k").getValue(), is("v"));
        assertThat(actual.getMapValue().get("k:qualified").getValue(), is("k:qualified"));
        assertThat(actual.getCollectionValue().size(), is(1));
        assertThat(actual.getCollectionValue().iterator().next().getValue(), is("foo"));
        assertThat(actual.getStringValue(), is("str"));
        assertTrue(actual.getBooleanValue());
        assertThat(actual.getIntegerValue(), is(1));
        assertThat(actual.getLongValue(), is(10L));
        assertThat(actual.getEnumValue(), is(YamlNodeRuleConfigurationEnum.FOO));
    }
    
    @Test
    void assertSwapToNotFoundYamlGlobalRuleConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new YamlRuleNodeTupleSwapperEngine().swapToYamlGlobalRuleConfiguration("invalid", "value: foo"));
    }
    
    @Test
    void assertSwapToYamlGlobalRuleConfiguration() {
        YamlRuleConfiguration actual = new YamlRuleNodeTupleSwapperEngine().swapToYamlGlobalRuleConfiguration("leaf", "value: foo");
        assertThat(((YamlLeafRuleConfiguration) actual).getValue(), is("foo"));
    }
}
