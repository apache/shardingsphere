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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        Collection<DataNode> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).getDataNodesByTableName("foo_tbl");
        assertThat(actual, is(Arrays.asList(new DataNode("foo_ds.foo_tbl"), new DataNode("bar_ds.foo_tbl"))));
    }
    
    @Test
    void assertFindFirstActualTable() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findFirstActualTable("foo_tbl");
        assertThat(actual, is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindFirstActualTable() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findFirstActualTable("no_tbl");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertIsNeedAccumulateWithEmptyTables() {
        assertTrue(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).isNeedAccumulate(Collections.emptyList()));
    }
    
    @Test
    void assertIsNeedAccumulate() {
        assertTrue(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).isNeedAccumulate(Collections.singleton("no_tbl")));
        assertFalse(new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).isNeedAccumulate(Arrays.asList("foo_tbl", "bar_tbl")));
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findLogicTableByActualTable("foo_tbl");
        assertThat(actual, is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindLogicTableByActualTable() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findLogicTableByActualTable("no_tbl");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindActualTableByCatalog() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findActualTableByCatalog("foo_ds", "foo_tbl");
        assertThat(actual, is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindActualTableByCatalogWithNotExistedCatalog() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findActualTableByCatalog("no_ds", "foo_tbl");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertNotFindActualTableByCatalogWithNotExistedTable() {
        Optional<String> actual = new BroadcastDataNodeRuleAttribute(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_tbl", "bar_tbl")).findActualTableByCatalog("foo_ds", "no_tbl");
        assertFalse(actual.isPresent());
    }
}
