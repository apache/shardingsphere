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

package org.apache.shardingsphere.sharding.metadata.reviser.schema;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.exception.kernel.metadata.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ShardingSchemaTableAggregationReviserTest {
    
    @Test
    void assertAggregateSuccessWithCheckTableMetaDataDisabled() {
        TableMetaData tableMetaData = new TableMetaData("foo_tbl", null, null, null);
        ShardingSchemaTableAggregationReviser reviser = new ShardingSchemaTableAggregationReviser(false);
        reviser.add(tableMetaData);
        Collection<TableMetaData> actual = reviser.aggregate(mock(ShardingRule.class));
        assertThat(actual, is(Collections.singletonList(tableMetaData)));
    }
    
    @Test
    void assertAggregateSuccessWithCheckTableMetaDataEnabled() {
        TableMetaData tableMetaData = new TableMetaData("foo_tbl", null, null, null);
        ShardingSchemaTableAggregationReviser reviser = new ShardingSchemaTableAggregationReviser(true);
        reviser.add(tableMetaData);
        Collection<TableMetaData> actual = reviser.aggregate(mock(ShardingRule.class));
        assertThat(actual, is(Collections.singletonList(tableMetaData)));
    }
    
    @Test
    void assertAggregateFailedWithCheckTableMetaDataEnabled() {
        TableMetaData tableMetaData1 = new TableMetaData("foo_tbl", null, null, null);
        TableMetaData tableMetaData2 = new TableMetaData("foo_tbl", Collections.singletonList(mock(ColumnMetaData.class)), null, null);
        ShardingSchemaTableAggregationReviser reviser = new ShardingSchemaTableAggregationReviser(true);
        reviser.add(tableMetaData1);
        reviser.add(tableMetaData2);
        assertThrows(RuleAndStorageMetaDataMismatchedException.class, () -> reviser.aggregate(mock(ShardingRule.class)));
    }
}
