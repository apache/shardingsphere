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

package org.apache.shardingsphere.sharding.rewrite.context;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSQLRewriteContextDecoratorTest {
    
    @Test
    void assertDecorate() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        when(sqlRewriteContext.getDatabase()).thenReturn(mock(ShardingSphereDatabase.class));
        when(sqlRewriteContext.getParameters()).thenReturn(Collections.singletonList(new Object()));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        new ShardingSQLRewriteContextDecorator().decorate(mock(ShardingRule.class), mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        assertTrue(sqlRewriteContext.getSqlTokens().isEmpty());
    }
    
    @Test
    void assertDecorateWhenInsertStatementNotContainsShardingTable() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        when(insertStatementContext.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.empty());
        new ShardingSQLRewriteContextDecorator().decorate(shardingRule, mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        assertTrue(sqlRewriteContext.getSqlTokens().isEmpty());
    }
}
