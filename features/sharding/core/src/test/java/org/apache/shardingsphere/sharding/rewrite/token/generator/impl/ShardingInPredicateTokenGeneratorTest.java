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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingInPredicateTokenGeneratorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingTable shardingTable;
    
    private ShardingInPredicateTokenGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new ShardingInPredicateTokenGenerator(shardingRule);
    }
    
    /**
     * Test that token is not generated for non-SELECT statements.
     */
    @Test
    void assertIsNotGenerateSQLTokenWithNonSelectStatement() {
        SQLStatementContext context = mock(SQLStatementContext.class);
        assertFalse(generator.isGenerateSQLToken(context));
    }
    
    /**
     * Test that token is not generated for non-sharding tables.
     */
    @Test
    void assertIsNotGenerateSQLTokenWithNonShardingTable() {
        InExpression inExpression = createInExpression("user_id", Arrays.asList(1, 2, 3));
        WhereSegment whereSegment = new WhereSegment(0, 10, inExpression);
        SQLStatementContext context = createSelectContext(whereSegment);
        
        when(shardingRule.isShardingTable("t_order")).thenReturn(false);
        
        assertFalse(generator.isGenerateSQLToken(context));
    }
    
    /**
     * Test that token is not generated for IN predicates on non-sharding columns.
     */
    @Test
    void assertIsNotGenerateSQLTokenWithNonShardingColumn() {
        InExpression inExpression = createInExpression("status", Arrays.asList(1, 2, 3));
        WhereSegment whereSegment = new WhereSegment(0, 10, inExpression);
        SQLStatementContext context = createSelectContext(whereSegment);
        
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.of(shardingTable));
        
        StandardShardingStrategyConfiguration strategyConfig = new StandardShardingStrategyConfiguration("user_id", "test_algorithm");
        when(shardingRule.getTableShardingStrategyConfiguration(shardingTable)).thenReturn(strategyConfig);
        
        assertFalse(generator.isGenerateSQLToken(context));
    }
    
    /**
     * Test that tokens are correctly generated for sharding key IN predicates.
     */
    @Test
    void assertGenerateSQLTokensWithShardingKeyInPredicate() {
        InExpression inExpression = createInExpression("user_id", Arrays.asList(1, 2, 3, 4, 5));
        WhereSegment whereSegment = new WhereSegment(0, 50, inExpression);
        SQLStatementContext context = createSelectContext(whereSegment);
        
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.of(shardingTable));
        
        StandardShardingStrategyConfiguration strategyConfig = new StandardShardingStrategyConfiguration("user_id", "mod_algorithm");
        when(shardingRule.getTableShardingStrategyConfiguration(shardingTable)).thenReturn(strategyConfig);
        
        // Verify isGenerateSQLToken returns true
        assertTrue(generator.isGenerateSQLToken(context));
        
        // Verify generateSQLTokens returns correct token
        Collection<SQLToken> tokens = generator.generateSQLTokens(context);
        
        assertThat(tokens.size(), is(1));
        SQLToken token = tokens.iterator().next();
        assertThat(token, instanceOf(ShardingInPredicateToken.class));
        
        ShardingInPredicateToken inToken = (ShardingInPredicateToken) token;
        assertThat(inToken.getStartIndex(), is(inExpression.getStartIndex()));
        assertThat(inToken.getStopIndex(), is(inExpression.getStopIndex()));
        assertThat(inToken.getColumnName(), is("user_id"));
    }
    
    /**
     * Test that no tokens are generated for empty IN lists.
     */
    @Test
    void assertGenerateSQLTokensWithEmptyInList() {
        InExpression inExpression = createInExpression("user_id", Collections.emptyList());
        WhereSegment whereSegment = new WhereSegment(0, 50, inExpression);
        SQLStatementContext context = createSelectContext(whereSegment);
        
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.of(shardingTable));
        
        StandardShardingStrategyConfiguration strategyConfig = new StandardShardingStrategyConfiguration("user_id", "mod_algorithm");
        when(shardingRule.getTableShardingStrategyConfiguration(shardingTable)).thenReturn(strategyConfig);
        
        Collection<SQLToken> tokens = generator.generateSQLTokens(context);
        
        assertTrue(tokens.isEmpty());
    }
    
    /**
     * Test that no tokens are generated for non-standard sharding strategies.
     */
    @Test
    void assertGenerateSQLTokensWithNonStandardStrategy() {
        InExpression inExpression = createInExpression("user_id", Arrays.asList(1, 2, 3));
        WhereSegment whereSegment = new WhereSegment(0, 50, inExpression);
        SQLStatementContext context = createSelectContext(whereSegment);
        
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.of(shardingTable));
        
        when(shardingRule.getTableShardingStrategyConfiguration(shardingTable))
                .thenReturn(mock(ShardingStrategyConfiguration.class));
        
        Collection<SQLToken> tokens = generator.generateSQLTokens(context);
        
        assertTrue(tokens.isEmpty());
    }
    
    /**
     * Create a mock SELECT statement context for testing.
     */
    private SQLStatementContext createSelectContext(final WhereSegment whereSegment) {
        SelectStatementContext context = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(context.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        
        if (whereSegment != null) {
            when(context.getWhereSegments()).thenReturn(Collections.singleton(whereSegment));
        } else {
            when(context.getWhereSegments()).thenReturn(Collections.emptyList());
        }
        
        return context;
    }
    
    /**
     * Create an IN expression for testing.
     */
    private InExpression createInExpression(final String columnName, final Collection<Integer> values) {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        
        ListExpression listExpression = new ListExpression(0, 0);
        for (Integer value : values) {
            listExpression.getItems().add(new LiteralExpressionSegment(0, 0, value));
        }
        
        return new InExpression(0, 50, column, listExpression, false);
    }
}
