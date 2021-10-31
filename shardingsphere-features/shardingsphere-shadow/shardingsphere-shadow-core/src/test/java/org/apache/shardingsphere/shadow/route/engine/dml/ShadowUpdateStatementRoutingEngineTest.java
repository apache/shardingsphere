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

import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowUpdateStatementRoutingEngineTest {
    
    private ShadowUpdateStatementRoutingEngine shadowRouteEngine;
    
    @Before
    public void init() {
        shadowRouteEngine = new ShadowUpdateStatementRoutingEngine(createUpdateStatementContext(), Collections.emptyList());
    }
    
    private UpdateStatementContext createUpdateStatementContext() {
        UpdateStatementContext result = mock(UpdateStatementContext.class);
        Collection<SimpleTableSegment> allTables = new LinkedList<>();
        allTables.add(new SimpleTableSegment(new TableNameSegment(20, 25, new IdentifierValue("t_order"))));
        when(result.getAllTables()).thenReturn(allTables);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, "1"));
        WhereSegment whereSegment = new WhereSegment(0, 0, binaryOperationExpression);
        when(result.getWhere()).thenReturn(Optional.of(whereSegment));
        MySQLUpdateStatement updateStatement = new MySQLUpdateStatement();
        Collection<CommentSegment> commentSegments = new LinkedList<>();
        commentSegments.add(new CommentSegment("/*shadow:true,foo:bar*/", 0, 20));
        commentSegments.add(new CommentSegment("/*aaa:bbb*/", 21, 30));
        updateStatement.setCommentSegments(commentSegments);
        when(result.getSqlStatement()).thenReturn(updateStatement);
        return result;
    }
    
    @Test
    public void assertRouteAndParseShadowColumnConditions() {
        RouteContext routeContext = mock(RouteContext.class);
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds", "ds_shadow"), new LinkedList<>()));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        shadowRouteEngine.route(routeContext, new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
        Optional<Collection<ShadowColumnCondition>> shadowColumnConditions = shadowRouteEngine.parseShadowColumnConditions();
        assertTrue(shadowColumnConditions.isPresent());
        Collection<ShadowColumnCondition> shadowColumns = shadowColumnConditions.get();
        assertThat(shadowColumns.size(), is(1));
        Iterator<ShadowColumnCondition> iterator = shadowColumns.iterator();
        ShadowColumnCondition userId = iterator.next();
        assertThat(userId.getColumn(), is("user_id"));
        assertThat(userId.getOwner(), is("t_order"));
        assertThat(userId.getValues().iterator().next(), is("1"));
        Optional<Collection<String>> sqlNotes = shadowRouteEngine.parseSqlNotes();
        assertTrue(sqlNotes.isPresent());
        assertThat(sqlNotes.get().size(), is(2));
        Iterator<String> sqlNotesIt = sqlNotes.get().iterator();
        assertThat(sqlNotesIt.next(), is("/*shadow:true,foo:bar*/"));
        assertThat(sqlNotesIt.next(), is("/*aaa:bbb*/"));
    }

    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setEnable(true);
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }

    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("user-id-update-regex-algorithm", createColumnShadowAlgorithm());
        return result;
    }

    private ShadowAlgorithm createColumnShadowAlgorithm() {
        Properties properties = new Properties();
        properties.setProperty("column", "user_id");
        properties.setProperty("operation", "update");
        properties.setProperty("regex", "[1]");
        ColumnRegexMatchShadowAlgorithm columnRegexMatchShadowAlgorithm = new ColumnRegexMatchShadowAlgorithm();
        columnRegexMatchShadowAlgorithm.setProps(properties);
        columnRegexMatchShadowAlgorithm.init();
        return columnRegexMatchShadowAlgorithm;
    }

    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        Collection<String> shadowAlgorithmNames = new LinkedList<>();
        shadowAlgorithmNames.add("user-id-update-regex-algorithm");
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), shadowAlgorithmNames));
        return result;
    }

    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        return result;
    }
    
    @Test
    public void assertGetAllTables() {
        Collection<SimpleTableSegment> allTables = shadowRouteEngine.getAllTables();
        assertThat(allTables.size(), is(1));
        assertThat(allTables.iterator().next().getTableName().getIdentifier().getValue(), is("t_order"));
    }
}
