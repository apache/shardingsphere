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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingRemoveTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertThat(sqlTokens.size(), is(1));
        assertThat(sqlTokens.iterator().next(), instanceOf(RemoveToken.class));
    }
    
    private static SelectStatementContext createUpdatesStatementContext() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC))));
        selectStatement.setProjections(createProjectionsSegment());
        return new SelectStatementContext(
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS)), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private static ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment result = new ProjectionsSegment(0, 8);
        result.getProjections().add(new AggregationDistinctProjectionSegment(0, 8, AggregationType.SUM, "number", "(DISTINCT number)"));
        return result;
    }
}
