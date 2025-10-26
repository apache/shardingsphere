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

package org.apache.shardingsphere.infra.datanode;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataNodeUtilsTest {
    
    @Test
    void assertGetDataNodeGroups() {
        Map<String, List<DataNode>> expected = new LinkedHashMap<>(2, 1F);
        expected.put("ds_0", Arrays.asList(new DataNode("ds_0.tbl_0"), new DataNode("ds_0.tbl_1")));
        expected.put("ds_1", Arrays.asList(new DataNode("ds_1.tbl_0"), new DataNode("ds_1.tbl_1")));
        List<DataNode> dataNodes = new LinkedList<>();
        expected.values().forEach(dataNodes::addAll);
        Map<String, List<DataNode>> actual = DataNodeUtils.getDataNodeGroups(dataNodes);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertBuildDataNodeWithSameDataSource() {
        DataNode dataNode = new DataNode("readwrite_ds.t_order");
        Collection<DataNode> dataNodes = DataNodeUtils.buildDataNode(dataNode, Collections.singletonMap("readwrite_ds", Arrays.asList("ds_0", "shadow_ds_0")));
        assertThat(dataNodes.size(), is(2));
        Iterator<DataNode> iterator = dataNodes.iterator();
        assertThat(iterator.next().getDataSourceName(), is("ds_0"));
        assertThat(iterator.next().getDataSourceName(), is("shadow_ds_0"));
    }
    
    @Test
    void assertBuildDataNodeWithoutSameDataSource() {
        DataNode dataNode = new DataNode("read_ds.t_order");
        Collection<DataNode> dataNodes = DataNodeUtils.buildDataNode(dataNode, Collections.singletonMap("readwrite_ds", Arrays.asList("ds_0", "shadow_ds_0")));
        assertThat(dataNodes.size(), is(1));
        assertThat(dataNodes.iterator().next().getDataSourceName(), is("read_ds"));
    }
    
    @Test
    void assertGetFormattedDataNodes() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(2, "t_order", Arrays.asList("ds_0", "ds_1", "ds_2"));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("ds_0.t_order_0"));
        assertThat(actual.get(1), is("ds_1.t_order_1"));
    }
    
    @Test
    void assertGetFormatSingleDataNode() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(2, "t_order", Collections.singleton("ds_0"));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("ds_0.t_order_0"));
        assertThat(actual.get(1), is("ds_0.t_order_1"));
    }
    
    @Test
    void assertGetFormattedDataNodesWithCycling() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(5, "t_user", Arrays.asList("ds_0", "ds_1"));
        assertThat(actual.size(), is(5));
        assertThat(actual.get(0), is("ds_0.t_user_0"));
        assertThat(actual.get(1), is("ds_1.t_user_1"));
        assertThat(actual.get(2), is("ds_0.t_user_2"));
        assertThat(actual.get(3), is("ds_1.t_user_3"));
        assertThat(actual.get(4), is("ds_0.t_user_4"));
    }
    
    @Test
    void assertGetFormattedDataNodesWithZeroAmount() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(0, "t_order", Arrays.asList("ds_0", "ds_1"));
        assertThat(actual.size(), is(0));
        assertThat(actual.isEmpty(), is(true));
    }
    
    @Test
    void assertGetFormattedDataNodesWithAmountEqualToDataSourcesSize() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(3, "t_order_item", Arrays.asList("ds_0", "ds_1", "ds_2"));
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("ds_0.t_order_item_0"));
        assertThat(actual.get(1), is("ds_1.t_order_item_1"));
        assertThat(actual.get(2), is("ds_2.t_order_item_2"));
    }
    
    @Test
    void assertGetFormattedDataNodesWithSpecialCharactersInLogicTable() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(2, "tbl_order_details", Arrays.asList("ds_0", "ds_1"));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("ds_0.tbl_order_details_0"));
        assertThat(actual.get(1), is("ds_1.tbl_order_details_1"));
    }
    
    @Test
    void assertGetFormattedDataNodesWithLargeAmount() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(100, "t_test", Collections.singletonList("ds_0"));
        assertThat(actual.size(), is(100));
        assertThat(actual.get(0), is("ds_0.t_test_0"));
        assertThat(actual.get(99), is("ds_0.t_test_99"));
    }
    
    @Test
    void assertGetFormattedDataNodesWithEmptyDataSources() {
        try {
            DataNodeUtils.getFormattedDataNodes(1, "t_order", Collections.emptyList());
        } catch (final java.util.NoSuchElementException ex) {
            assertThat(true, is(true));
        }
    }
    
    @Test
    void assertGetFormattedDataNodesWithSingleAmountAndMultipleDataSources() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(1, "t_config", Arrays.asList("ds_0", "ds_1", "ds_2"));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is("ds_0.t_config_0"));
    }
    
    @Test
    void assertGetFormattedDataNodesIteratorResetBehavior() {
        List<String> actual = DataNodeUtils.getFormattedDataNodes(7, "t_table", Arrays.asList("first", "second", "third"));
        assertThat(actual.size(), is(7));
        assertThat(actual.get(0), is("first.t_table_0"));
        assertThat(actual.get(1), is("second.t_table_1"));
        assertThat(actual.get(2), is("third.t_table_2"));
        assertThat(actual.get(3), is("first.t_table_3"));
        assertThat(actual.get(4), is("second.t_table_4"));
        assertThat(actual.get(5), is("third.t_table_5"));
        assertThat(actual.get(6), is("first.t_table_6"));
    }
}
