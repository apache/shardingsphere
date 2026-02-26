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

package org.apache.shardingsphere.broadcast.metadata.reviser.schema;

import lombok.SneakyThrows;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BroadcastSchemaTableAggregationReviserTest {
    
    @Test
    void assertAdd() {
        BroadcastSchemaTableAggregationReviser reviser = new BroadcastSchemaTableAggregationReviser(false);
        TableMetaData tableMetaData = new TableMetaData("foo_tbl", null, null, null);
        reviser.add(tableMetaData);
        Map<String, Collection<TableMetaData>> actual = getTableMetaDataMap(reviser);
        assertTrue(actual.containsKey("foo_tbl"));
        assertThat(actual.get("foo_tbl"), is(Collections.singletonList(tableMetaData)));
    }
    
    @Test
    void assertAggregateWithCheckTableMetaDataDisabled() {
        BroadcastSchemaTableAggregationReviser reviser = new BroadcastSchemaTableAggregationReviser(false);
        Map<String, Collection<TableMetaData>> tableMetaDataMap = getTableMetaDataMap(reviser);
        Collection<TableMetaData> tableMetaDataList = createMismatchedTableMetaDataList();
        tableMetaDataMap.put("foo_tbl", tableMetaDataList);
        Collection<TableMetaData> actual = reviser.aggregate(mock(BroadcastRule.class));
        assertThat(actual, is(Collections.singletonList(tableMetaDataList.iterator().next())));
    }
    
    @Test
    void assertAggregateWithCheckTableMetaDataEnabled() {
        BroadcastSchemaTableAggregationReviser reviser = new BroadcastSchemaTableAggregationReviser(true);
        Map<String, Collection<TableMetaData>> tableMetaDataMap = getTableMetaDataMap(reviser);
        tableMetaDataMap.put("foo_tbl", createMismatchedTableMetaDataList());
        assertThrows(RuleAndStorageMetaDataMismatchedException.class, () -> reviser.aggregate(mock(BroadcastRule.class)));
    }
    
    private Collection<TableMetaData> createMismatchedTableMetaDataList() {
        TableMetaData tableMetaData1 = new TableMetaData("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        TableMetaData tableMetaData2 = new TableMetaData("foo_tbl", Collections.singletonList(mock(ColumnMetaData.class)), Collections.emptyList(), Collections.emptyList());
        return Arrays.asList(tableMetaData1, tableMetaData2);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Map<String, Collection<TableMetaData>> getTableMetaDataMap(final BroadcastSchemaTableAggregationReviser reviser) {
        return (Map<String, Collection<TableMetaData>>) Plugins.getMemberAccessor().get(BroadcastSchemaTableAggregationReviser.class.getDeclaredField("tableMetaDataMap"), reviser);
    }
}
