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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingAggregationDistinctTokenGeneratorTest {
    
    private final ShardingAggregationDistinctTokenGenerator generator = new ShardingAggregationDistinctTokenGenerator();
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyAggregationDistinctProjection() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getAggregationDistinctProjections().isEmpty()).thenReturn(true);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithAggregationDistinctProjections() {
        assertTrue(generator.isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertGenerateSQLTokenWithDerivedAlias() {
        AggregationDistinctProjection aggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue("AVG_DERIVED_COUNT_0")));
        when(aggregationDistinctProjection.getDistinctInnerExpression()).thenReturn("TEST_DISTINCT_INNER_EXPRESSION");
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singleton(aggregationDistinctProjection));
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(selectStatementContext));
        assertThat(actual.get(0).toString(), is("TEST_DISTINCT_INNER_EXPRESSION AS AVG_DERIVED_COUNT_0"));
    }
    
    @Test
    void assertGenerateSQLToken() {
        AggregationDistinctProjection aggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(aggregationDistinctProjection.getDistinctInnerExpression()).thenReturn("TEST_DISTINCT_INNER_EXPRESSION");
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singleton(aggregationDistinctProjection));
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue("TEST_ERROR_ALIAS")));
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(selectStatementContext));
        assertThat(actual.get(0).toString(), is("TEST_DISTINCT_INNER_EXPRESSION"));
    }
    
    @Test
    void assertGenerateSQLTokenWithExpressionDerivedAggregationDistinctProjection() {
        FunctionSegment functionSegment =
                new FunctionSegment(0, 32, "COALESCE", "COALESCE(SUM(DISTINCT price), 0)");
        AggregationDistinctProjectionSegment sumSegment =
                new AggregationDistinctProjectionSegment(9, 27, AggregationType.SUM, "SUM(DISTINCT price)", "price");
        LiteralExpressionSegment literalSegment =
                new LiteralExpressionSegment(31, 31, 0);
        functionSegment.getParameters().add(sumSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment =
                new ExpressionProjectionSegment(0, 32, "COALESCE(SUM(DISTINCT price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 32);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"))
                .projections(projectionsSegment)
                .from(new SimpleTableSegment(
                        new TableNameSegment(39, 45, new IdentifierValue("t_order"))))
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(selectStatementContext));
        assertFalse(actual.isEmpty());
        assertThat(actual.get(0).toString(), is("price AS EXPR_DERIVED_0"));
    }
    
    @Test
    void assertFinalSQLTokenGenerationWithExpressionDerivedDistinctAggregation() {
        FunctionSegment functionSegment = new FunctionSegment(0, 32, "COALESCE", "COALESCE(SUM(DISTINCT price), 0)");
        AggregationDistinctProjectionSegment sumSegment =
                new AggregationDistinctProjectionSegment(9, 27, AggregationType.SUM, "SUM(DISTINCT price)", "price");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(31, 31, 0);
        functionSegment.getParameters().add(sumSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment =
                new ExpressionProjectionSegment(0, 32, "COALESCE(SUM(DISTINCT price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 32);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"))
                .projections(projectionsSegment)
                .from(new SimpleTableSegment(new TableNameSegment(39, 45, new IdentifierValue("t_order"))))
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement,
                new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()),
                "foo_db",
                Collections.emptyList());
        
        String sql = "SELECT COALESCE(SUM(DISTINCT price), 0) FROM t_order";
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn(sql);
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        
        SQLRewriteContext rewriteContext = new SQLRewriteContext(database, queryContext);
        
        rewriteContext.addSQLTokenGenerators(Collections.singletonList(new ShardingAggregationDistinctTokenGenerator()));
        rewriteContext.generateSQLTokens();
        
        Collection<SQLToken> finalTokens = rewriteContext.getSqlTokens();
        
        assertFalse(finalTokens.isEmpty(), "Token generator was skipped by the engine pipeline!");
        
        boolean hasProjectionToken = finalTokens.stream().anyMatch(token -> token.toString().contains("price AS EXPR_DERIVED_0"));
        assertTrue(hasProjectionToken, "The final token generation pipeline is missing the rewritten distinct token expression.");
    }
}
