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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenUtilsTest {
    
    @Test
    void assertGetLogicAndActualTablesFromRouteUnit() {
        Map<String, String> actual = TokenUtils.getLogicAndActualTableMap(getRouteUnit(), mockSQLStatementContext(), mockShardingRule());
        assertThat(actual.get("foo_table"), is("foo_table_0"));
        assertThat(actual.get("bar_table"), is("bar_table_0"));
    }
    
    private RouteUnit getRouteUnit() {
        return new RouteUnit(new RouteMapper(DefaultDatabase.LOGIC_NAME, "ds_0"), Collections.singleton(new RouteMapper("foo_table", "foo_table_0")));
    }
    
    private static SQLStatementContext mockSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(Arrays.asList("foo_table", "bar_table"));
        return result;
    }
    
    private static ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getLogicAndActualTablesFromBindingTable(DefaultDatabase.LOGIC_NAME, "foo_table", "foo_table_0", Arrays.asList("foo_table", "bar_table")))
                .thenReturn(Collections.singletonMap("bar_table", "bar_table_0"));
        return result;
    }
}
