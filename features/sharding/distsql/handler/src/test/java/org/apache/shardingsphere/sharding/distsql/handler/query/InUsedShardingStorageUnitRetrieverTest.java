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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InUsedShardingStorageUnitRetrieverTest {
    
    @SuppressWarnings("unchecked")
    private final InUsedStorageUnitRetriever<ShardingRule> retriever = TypedSPILoader.getService(InUsedStorageUnitRetriever.class, ShardingRule.class);
    
    @Test
    void assertGetInUsedResources() {
        ShowRulesUsedStorageUnitStatement sqlStatement = new ShowRulesUsedStorageUnitStatement("foo_ds", null);
        assertThat(retriever.getInUsedResources(sqlStatement, mockRule()), is(Collections.singleton("foo_tbl")));
    }
    
    private ShardingRule mockRule() {
        ShardingRule result = mock(ShardingRule.class);
        ShardingTable fooTbl = mock(ShardingTable.class);
        when(fooTbl.getLogicTable()).thenReturn("foo_tbl");
        when(fooTbl.getActualDataSourceNames()).thenReturn(Arrays.asList("foo_ds", "bar_ds"));
        when(result.getShardingTables()).thenReturn(Collections.singletonMap("foo_tbl", fooTbl));
        return result;
    }
}
