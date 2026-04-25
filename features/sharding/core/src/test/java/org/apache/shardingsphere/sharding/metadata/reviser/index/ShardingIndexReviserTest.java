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
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
        assertThat(new ShardingIndexReviser(mock(ShardingTable.class)).revise("foo_tbl", new IndexMetaData("foo_idx"), mock(ShardingRule.class)), is(Optional.empty()));
    }
    
    @Test
    void assertReviseWithHashedActualIndexName() {
        Optional<IndexMetaData> actual = new ShardingIndexReviser(mockShardingTable())
                .revise("tbl_0", new IndexMetaData(IndexMetaDataUtils.getActualIndexName("named_index_boundary_case_abcdefghijklmnopqrstuvwxyz", "tbl_0", databaseType),
                        Collections.singletonList("foo_col")), mock(ShardingRule.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("named_index_boundary_case_abcdefghijklmnopqrstuvwxyz"));
        assertThat(actual.get().getColumns().size(), is(1));
    }
    
    @Test
    void assertReviseWithLegacyActualIndexName() {
        Optional<IndexMetaData> actual = new ShardingIndexReviser(mockShardingTable())
                .revise("tbl_0", new IndexMetaData(IndexMetaDataUtils.getLegacyActualIndexName("foo_idx", "tbl_0"), Collections.singletonList("foo_col")), mock(ShardingRule.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_idx"));
    }
    
    @Test
    void assertReviseWithLegacyActualIndexNameForSecondActualTable() {
        Optional<IndexMetaData> actual = new ShardingIndexReviser(mockShardingTable())
                .revise("tbl_1", new IndexMetaData(IndexMetaDataUtils.getLegacyActualIndexName("foo_idx", "tbl_1"), Collections.singletonList("foo_col")), mock(ShardingRule.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_idx"));
    }
    
    @Test
    void assertReviseWithTruncatedAnonymousActualIndexName() {
        String logicIndexName = "very_long_status_column_name_for_sharding_rewrite_validation_account_identifier_idx";
        Optional<IndexMetaData> actual = new ShardingIndexReviser(mockShardingTable())
                .revise("tbl_0", new IndexMetaData(IndexMetaDataUtils.getActualIndexName(logicIndexName, "tbl_0", databaseType),
                        Arrays.asList("very_long_status_column_name_for_sharding_rewrite_validation", "account_identifier")), mock(ShardingRule.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is(logicIndexName));
    }
    
    private static ShardingTable mockShardingTable() {
        ShardingTable result = mock(ShardingTable.class);
        when(result.getActualDataNodes()).thenReturn(Arrays.asList(new DataNode("foo_schema", (String) null, "tbl_0"), new DataNode("foo_schema", (String) null, "tbl_1")));
        return result;
    }
}
