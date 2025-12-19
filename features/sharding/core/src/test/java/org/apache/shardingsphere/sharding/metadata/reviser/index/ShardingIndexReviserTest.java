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

import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingIndexReviserTest {
    
    @Test
    void assertReviseWithEmptyActualDataNode() {
        assertThat(new ShardingIndexReviser(mock(ShardingTable.class)).revise("foo_tbl", new IndexMetaData("foo_idx"), mock(ShardingRule.class)), is(Optional.empty()));
    }
    
    @Test
    void assertReviseWithActualDataNodes() {
        Optional<IndexMetaData> actual = new ShardingIndexReviser(mockShardingTable()).revise("tbl_0", new IndexMetaData("foo_idx", Collections.singletonList("foo_col")), mock(ShardingRule.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_idx"));
        assertThat(actual.get().getColumns().size(), is(1));
    }
    
    private static ShardingTable mockShardingTable() {
        ShardingTable result = mock(ShardingTable.class);
        when(result.getActualDataNodes()).thenReturn(Arrays.asList(new DataNode("foo_schema", (String) null, "tbl_0"), new DataNode("foo_schema", (String) null, "tbl_1")));
        return result;
    }
}
