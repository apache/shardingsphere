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

package org.apache.shardingsphere.single.rule.attribute;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SingleDataNodeRuleAttributeTest {
    
    private final Collection<DataNode> fooDataNodes = Collections.singleton(new DataNode("foo_ds.foo_table"));
    
    private final Map<String, Collection<DataNode>> tableDataNodes = Collections.singletonMap("foo_table", fooDataNodes);
    
    private final SingleDataNodeRuleAttribute ruleAttribute = new SingleDataNodeRuleAttribute(tableDataNodes);
    
    @Test
    void assertGetAllDataNodes() {
        Map<String, Collection<DataNode>> actualDataNodes = ruleAttribute.getAllDataNodes();
        assertThat(actualDataNodes, is(tableDataNodes));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDataNodesByTableNameArguments")
    void assertGetDataNodesByTableName(final String name, final String tableName, final Collection<DataNode> expectedDataNodes) {
        assertThat(ruleAttribute.getDataNodesByTableName(tableName), is(expectedDataNodes));
    }
    
    @Test
    void assertFindFirstActualTable() {
        assertFalse(ruleAttribute.findFirstActualTable("foo_table").isPresent());
    }
    
    @Test
    void assertIsNeedAccumulate() {
        assertFalse(ruleAttribute.isNeedAccumulate(Collections.singleton("foo_table")));
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        assertFalse(ruleAttribute.findLogicTableByActualTable("foo_table").isPresent());
    }
    
    @Test
    void assertFindActualTableByCatalog() {
        assertFalse(ruleAttribute.findActualTableByCatalog("foo_catalog", "foo_table").isPresent());
    }
    
    @Test
    void assertIsReplicaBasedDistribution() {
        assertFalse(ruleAttribute.isReplicaBasedDistribution());
    }
    
    private static Stream<Arguments> getDataNodesByTableNameArguments() {
        Collection<DataNode> expectedDataNodes = Collections.singleton(new DataNode("foo_ds.foo_table"));
        return Stream.of(
                Arguments.of("table name hit", "foo_table", expectedDataNodes),
                Arguments.of("table name hit with upper case", "FOO_TABLE", expectedDataNodes),
                Arguments.of("table name miss", "bar_table", Collections.emptyList()));
    }
}
