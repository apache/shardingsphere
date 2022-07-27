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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingRemoveTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

public final class ShardingRemoveTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLTokenWithNonSelectStatement() {
        assertFalse(new ShardingRemoveTokenGenerator().isGenerateSQLToken(mock(InsertStatementContext.class)));
    }
    
    @Test
    public void assertIsGenerateSQLTokenWithEmptyAggregationDistinctProjections() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections().isEmpty()).thenReturn(true);
        assertFalse(new ShardingRemoveTokenGenerator().isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    public void assertIsGenerateSQLTokenWithNonEmptyAggregationDistinctProjections() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        assertTrue(new ShardingRemoveTokenGenerator().isGenerateSQLToken(selectStatementContext));
    }

    @Test
    public void assertGenerateSQLTokens() {
        Collection<? extends SQLToken> sqlTokens = new ShardingRemoveTokenGenerator().generateSQLTokens(createUpdatesStatementContext());

    }

    protected static SelectStatementContext createUpdatesStatementContext() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setTable(createTableSegment("t_user"));
        selectStatement.setWhere(createWhereSegment());
        selectStatement.setGroupBy(createGroupBySegment());
        return new SelectStatementContext(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(ShardingSphereDatabase.class)), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }

    private static WhereSegment createWhereSegment() {
        BinaryOperationExpression nameExpression = new BinaryOperationExpression(10, 24,
                new ColumnSegment(10, 13, new IdentifierValue("name")), new LiteralExpressionSegment(18, 22, "LiLei"), "=", "name = 'LiLei'");
        BinaryOperationExpression pwdExpression = new BinaryOperationExpression(30, 44,
                new ColumnSegment(30, 32, new IdentifierValue("pwd")), new LiteralExpressionSegment(40, 45, "123456"), "=", "pwd = '123456'");
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, nameExpression, pwdExpression, "AND", "name = 'LiLei' AND pwd = '123456'"));
    }

    private static SimpleTableSegment createTableSegment(final String tableName) {
        return new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
    }

    private static GroupBySegment createGroupBySegment() {
        return new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC)));
    }
}
