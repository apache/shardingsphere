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

package org.apache.shardingsphere.broadcast.rule.attribute;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastDataNodeRuleAttributeTest {
    
    @Test
    void assertGetAllDataNodes() {
        Map<String, Collection<DataNode>> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).getAllDataNodes();
        assertThat(actual.size(), is(2));
        assertThat(actual.get("foo_tbl"), is(Arrays.asList(new DataNode("foo_ds.foo_tbl"), new DataNode("bar_ds.foo_tbl"))));
        assertThat(actual.get("bar_tbl"), is(Arrays.asList(new DataNode("foo_ds.bar_tbl"), new DataNode("bar_ds.bar_tbl"))));
    }
    
    @Test
    void assertGetDataNodesByTableName() {
        assertThat(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).getDataNodesByTableName("foo_tbl"),
                is(Arrays.asList(new DataNode("foo_ds.foo_tbl"), new DataNode("bar_ds.foo_tbl"))));
    }
    
    @Test
    void assertFindFirstActualTable() {
        assertThat(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findFirstActualTable("foo_tbl"), is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindFirstActualTable() {
        assertFalse(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findFirstActualTable("no_tbl").isPresent());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getIsNeedAccumulateArguments")
    void assertIsNeedAccumulate(final String name, final Collection<String> tables, final boolean expected) {
        assertThat(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).isNeedAccumulate(tables), is(expected));
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        assertThat(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findLogicTableByActualTable("foo_tbl"), is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindLogicTableByActualTable() {
        assertFalse(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findLogicTableByActualTable("no_tbl").isPresent());
    }
    
    @Test
    void assertIsReplicaBasedDistribution() {
        assertTrue(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).isReplicaBasedDistribution());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getFindActualTableByCatalogArguments")
    void assertFindActualTableByCatalog(final String name, final String catalog, final String logicTable, final String expected) {
        assertThat(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findActualTableByCatalog(catalog, logicTable),
                is(Optional.ofNullable(expected)));
    }
    
    private static Stream<Arguments> getIsNeedAccumulateArguments() {
        return Stream.of(
                Arguments.of("empty tables", Collections.emptyList(), true),
                Arguments.of("table not found", Collections.singleton("no_tbl"), true),
                Arguments.of("all tables are included", Arrays.asList("foo_tbl", "bar_tbl"), false));
    }
    
    private static Stream<Arguments> getFindActualTableByCatalogArguments() {
        return Stream.of(
                Arguments.of("matched data source and table", "foo_ds", "foo_tbl", "foo_tbl"),
                Arguments.of("unmatched data source", "no_ds", "foo_tbl", null),
                Arguments.of("table not found", "foo_ds", "no_tbl", null));
    }
}
