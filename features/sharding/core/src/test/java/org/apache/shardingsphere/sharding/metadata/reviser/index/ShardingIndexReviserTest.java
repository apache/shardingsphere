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

package org.apache.shardingsphere.sharding.metadata.reviser.index;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingIndexReviserTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertReviseWithEmptyActualDataNode() {
        assertThat(new ShardingIndexReviser(mock(ShardingTable.class)).revise("foo_tbl", new IndexMetaData("foo_idx"), Collections.emptyList(), mock(ShardingRule.class)),
                is(Optional.empty()));
    }
    
    @Test
    void assertReviseWithHashedActualIndexName() {
        Optional<IndexMetaData> actual = revise("tbl_0", new IndexMetaData(IndexMetaDataUtils.getActualIndexName(
                "named_index_boundary_case_abcdefghijklmnopqrstuvwxyz", "tbl_0", databaseType), Collections.singletonList("foo_col")));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("named_index_boundary_case_abcdefghijklmnopqrstuvwxyz"));
        assertThat(actual.get().getColumns().size(), is(1));
    }
    
    @Test
    void assertReviseWithTruncatedNamedActualIndexNameUsingCandidates() {
        String logicIndexName = "named_index_boundary_case_abcdefghijklmnopqrstuvwxyz_0123456789";
        IndexMetaData truncatedActualIndexMetaData = new IndexMetaData(
                IndexMetaDataUtils.getActualIndexName(logicIndexName, "tbl_0", databaseType), Collections.singletonList("foo_col"));
        IndexMetaData legacyActualIndexMetaData = new IndexMetaData(IndexMetaDataUtils.getLegacyActualIndexName(logicIndexName, "tbl_1"), Collections.singletonList("foo_col"));
        Optional<IndexMetaData> actual = revise("tbl_0", truncatedActualIndexMetaData,
                tableMetaData("tbl_0", truncatedActualIndexMetaData), tableMetaData("tbl_1", legacyActualIndexMetaData));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is(logicIndexName));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertReviseThroughSharedIndexReviseEngineWithTruncatedNamedActualIndexName() {
        String logicIndexName = "named_index_boundary_case_abcdefghijklmnopqrstuvwxyz_0123456789";
        IndexMetaData truncatedActualIndexMetaData = new IndexMetaData(
                IndexMetaDataUtils.getActualIndexName(logicIndexName, "tbl_0", databaseType), Collections.singletonList("foo_col"));
        IndexMetaData legacyActualIndexMetaData = new IndexMetaData(IndexMetaDataUtils.getLegacyActualIndexName(logicIndexName, "tbl_1"), Collections.singletonList("foo_col"));
        ShardingRule rule = mock(ShardingRule.class);
        MetaDataReviseEntry reviseEntry = mock(MetaDataReviseEntry.class);
        ShardingIndexReviser indexReviser = new ShardingIndexReviser(mockShardingTable());
        when(reviseEntry.getIndexReviser(rule, "tbl_0")).thenReturn(Optional.of(indexReviser));
        Collection<IndexMetaData> actual = new IndexReviseEngine<>(rule, reviseEntry).revise("tbl_0", Collections.singleton(truncatedActualIndexMetaData),
                Arrays.asList(tableMetaData("tbl_0", truncatedActualIndexMetaData), tableMetaData("tbl_1", legacyActualIndexMetaData)));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is(logicIndexName));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertReviseThroughSharedIndexReviseEngineWithAllTruncatedNamedActualIndexNamesUsingRevisionCandidates() {
        String logicIndexName = "named_index_boundary_case_abcdefghijklmnopqrstuvwxyz_0123456789";
        IndexMetaData firstTruncatedActualIndexMetaData = new IndexMetaData(
                IndexMetaDataUtils.getActualIndexName(logicIndexName, "tbl_0", databaseType), Collections.singletonList("foo_col"));
        IndexMetaData secondTruncatedActualIndexMetaData = new IndexMetaData(
                IndexMetaDataUtils.getActualIndexName(logicIndexName, "tbl_1", databaseType), Collections.singletonList("foo_col"));
        ShardingRule rule = mock(ShardingRule.class);
        MetaDataReviseEntry reviseEntry = mock(MetaDataReviseEntry.class);
        ShardingIndexReviser indexReviser = new ShardingIndexReviser(mockShardingTable());
        when(reviseEntry.getIndexReviser(rule, "tbl_0")).thenReturn(Optional.of(indexReviser));
        Collection<IndexMetaData> actual = new IndexReviseEngine<>(rule, reviseEntry).revise("tbl_0", Collections.singleton(firstTruncatedActualIndexMetaData),
                Arrays.asList(tableMetaData("tbl_0", firstTruncatedActualIndexMetaData), tableMetaData("tbl_1", secondTruncatedActualIndexMetaData)),
                Collections.singleton(tableMetaData("tbl", new IndexMetaData(logicIndexName, Collections.singletonList("foo_col")))));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is(logicIndexName));
    }
    
    @Test
    void assertReviseWithExactHashLikeIndexName() {
        Optional<IndexMetaData> actual = revise("tbl_0", new IndexMetaData("foo_h12345678", Collections.singletonList("foo_col")));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_h12345678"));
    }
    
    @Test
    void assertReviseWithExactTruncationLikeIndexName() {
        Optional<IndexMetaData> actual = revise("tbl_0", new IndexMetaData("foo_t12345678", Collections.singletonList("foo_col")));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_t12345678"));
    }
    
    @Test
    void assertReviseWithLegacyActualIndexName() {
        Optional<IndexMetaData> actual = revise("tbl_0",
                new IndexMetaData(IndexMetaDataUtils.getLegacyActualIndexName("foo_idx", "tbl_0"), Collections.singletonList("foo_col")));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_idx"));
    }
    
    @Test
    void assertReviseWithLegacyActualIndexNameForSecondActualTable() {
        Optional<IndexMetaData> actual = revise("tbl_1",
                new IndexMetaData(IndexMetaDataUtils.getLegacyActualIndexName("foo_idx", "tbl_1"), Collections.singletonList("foo_col")));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_idx"));
    }
    
    @Test
    void assertReviseWithTruncatedAnonymousActualIndexName() {
        String logicIndexName = "very_long_status_column_name_for_sharding_rewrite_validation_account_identifier_idx";
        Optional<IndexMetaData> actual = revise("tbl_0", new IndexMetaData(IndexMetaDataUtils.getActualIndexName(logicIndexName, "tbl_0", databaseType),
                Arrays.asList("very_long_status_column_name_for_sharding_rewrite_validation", "account_identifier")));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is(logicIndexName));
    }
    
    private Optional<IndexMetaData> revise(final String tableName, final IndexMetaData originalMetaData, final TableMetaData... originalTableMetaDataList) {
        return new ShardingIndexReviser(mockShardingTable()).revise(tableName, originalMetaData,
                0 == originalTableMetaDataList.length ? Collections.singleton(tableMetaData(tableName, originalMetaData)) : Arrays.asList(originalTableMetaDataList),
                mock(ShardingRule.class));
    }
    
    private static TableMetaData tableMetaData(final String tableName, final IndexMetaData... indexes) {
        return new TableMetaData(tableName, Collections.emptyList(), Arrays.asList(indexes), Collections.emptyList());
    }
    
    private static ShardingTable mockShardingTable() {
        ShardingTable result = mock(ShardingTable.class);
        when(result.getLogicTable()).thenReturn("tbl");
        when(result.getActualDataNodes()).thenReturn(Arrays.asList(new DataNode("foo_schema", (String) null, "tbl_0"), new DataNode("foo_schema", (String) null, "tbl_1")));
        return result;
    }
}
