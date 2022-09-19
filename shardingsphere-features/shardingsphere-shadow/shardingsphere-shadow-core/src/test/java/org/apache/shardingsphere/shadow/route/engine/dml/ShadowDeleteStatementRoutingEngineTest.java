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

package org.apache.shardingsphere.shadow.route.engine.dml;

import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowDeleteStatementRoutingEngineTest {
    
    private ShadowDeleteStatementRoutingEngine shadowDeleteStatementRoutingEngine;
    
    @Before
    public void init() {
        shadowDeleteStatementRoutingEngine = new ShadowDeleteStatementRoutingEngine(createDeleteStatementContext(), Collections.emptyList());
    }
    
    private DeleteStatementContext createDeleteStatementContext() {
        DeleteStatementContext result = mock(DeleteStatementContext.class);
        Collection<SimpleTableSegment> allTables = new LinkedList<>();
        allTables.add(new SimpleTableSegment(new TableNameSegment(20, 25, new IdentifierValue("t_order"))));
        when(result.getAllTables()).thenReturn(allTables);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, "1"));
        WhereSegment whereSegment = new WhereSegment(0, 0, binaryOperationExpression);
        when(result.getWhereSegments()).thenReturn(Collections.singletonList(whereSegment));
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        deleteStatement.getCommentSegments().add(new CommentSegment("/*shadow:true,foo:bar*/", 0, 20));
        deleteStatement.getCommentSegments().add(new CommentSegment("/*aaa:bbb*/", 21, 30));
        when(result.getSqlStatement()).thenReturn(deleteStatement);
        return result;
    }
    
    @Test
    public void assertRouteAndParseShadowColumnConditions() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(Collections.singleton(new RouteUnit(new RouteMapper("ds", "ds_shadow"), Collections.emptyList())));
        shadowDeleteStatementRoutingEngine.route(routeContext, new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
        Optional<Collection<String>> sqlNotes = shadowDeleteStatementRoutingEngine.parseSQLComments();
        assertTrue(sqlNotes.isPresent());
        assertThat(sqlNotes.get().size(), is(2));
        Iterator<String> sqlNotesIt = sqlNotes.get().iterator();
        assertThat(sqlNotesIt.next(), is("/*shadow:true,foo:bar*/"));
        assertThat(sqlNotesIt.next(), is("/*aaa:bbb*/"));
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(Collections.singletonMap("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow")));
        result.setTables(
                Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), Collections.singleton("user-id-delete-regex-algorithm"))));
        result.setShadowAlgorithms(Collections.singletonMap("user-id-delete-regex-algorithm", createShadowAlgorithm()));
        return result;
    }
    
    private ShadowAlgorithm createShadowAlgorithm() {
        return ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("REGEX_MATCH", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("column", "user_id");
        result.setProperty("operation", "delete");
        result.setProperty("regex", "[1]");
        return result;
    }
    
    @Test
    public void assertGetAllTables() {
        Collection<SimpleTableSegment> allTables = shadowDeleteStatementRoutingEngine.getAllTables();
        assertThat(allTables.size(), is(1));
        assertThat(allTables.iterator().next().getTableName().getIdentifier().getValue(), is("t_order"));
    }
}
