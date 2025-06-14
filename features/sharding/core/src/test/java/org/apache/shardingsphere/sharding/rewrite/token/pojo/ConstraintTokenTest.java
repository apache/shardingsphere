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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstraintTokenTest {
    
    @Test
    void assertToString() {
        assertThat(new ConstraintToken(0, 1, new IdentifierValue("uc"), mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), mock(ShardingRule.class)).toString(getRouteUnit()), is("uc"));
    }
    
    @Test
    void assertUpperCaseToString() {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext();
        assertThat(new ConstraintToken(0, 1, new IdentifierValue("uc"), sqlStatementContext, mock(ShardingRule.class)).toString(getRouteUnit()), is("uc_t_order_0"));
    }
    
    private SQLStatementContext mockSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("T_ORDER"));
        return result;
    }
    
    private RouteUnit getRouteUnit() {
        return new RouteUnit(new RouteMapper("logic_db", "logic_db"), Collections.singletonList(new RouteMapper("t_order", "t_order_0")));
    }
}
