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

import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowInsertStatementRoutingEngineTest {
    
    private ShadowInsertStatementRoutingEngine shadowRouteEngine;
    
    @Before
    public void init() {
        shadowRouteEngine = new ShadowInsertStatementRoutingEngine(createInsertStatementContext());
    }
    
    private InsertStatementContext createInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class);
        when(result.getAllTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(20, 25, new IdentifierValue("t_order")))));
        when(result.getInsertColumnNames()).thenReturn(Arrays.asList("user_id", "order_code", "order_name"));
        List<ExpressionSegment> valueExpressions = Arrays.asList(new LiteralExpressionSegment(0, 10, "1"),
                new LiteralExpressionSegment(11, 20, "orderCode"), new LiteralExpressionSegment(21, 30, "orderName"));
        when(result.getInsertValueContexts()).thenReturn(Collections.singletonList(new InsertValueContext(valueExpressions, Collections.emptyList(), 0)));
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.getCommentSegments().add(new CommentSegment("/*shadow:true,foo:bar*/", 0, 20));
        insertStatement.getCommentSegments().add(new CommentSegment("/*aaa:bbb*/", 21, 30));
        when(result.getSqlStatement()).thenReturn(insertStatement);
        return result;
    }
    
    @Test
    public void assertRouteAndParseShadowColumnConditions() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(Collections.singleton(new RouteUnit(new RouteMapper("ds", "ds_shadow"), Collections.emptyList())));
        shadowRouteEngine.route(routeContext, new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
        Optional<Collection<String>> sqlNotes = shadowRouteEngine.parseSQLComments();
        assertTrue(sqlNotes.isPresent());
        assertThat(sqlNotes.get().size(), is(2));
        Iterator<String> sqlNotesIt = sqlNotes.get().iterator();
        assertThat(sqlNotesIt.next(), is("/*shadow:true,foo:bar*/"));
        assertThat(sqlNotesIt.next(), is("/*aaa:bbb*/"));
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(Collections.singletonMap("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singleton("shadow-data-source-0"), Collections.singleton("user-id-insert-regex-algorithm"))));
        result.setShadowAlgorithms(Collections.singletonMap("user-id-insert-regex-algorithm", createShadowAlgorithm()));
        return result;
    }
    
    private ShadowAlgorithm createShadowAlgorithm() {
        return ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("REGEX_MATCH", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("column", "user_id");
        result.setProperty("operation", "insert");
        result.setProperty("regex", "[1]");
        return result;
    }
    
    @Test
    public void assertGetAllTables() {
        Collection<SimpleTableSegment> actual = shadowRouteEngine.getAllTables();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getTableName().getIdentifier().getValue(), is("t_order"));
    }
}
