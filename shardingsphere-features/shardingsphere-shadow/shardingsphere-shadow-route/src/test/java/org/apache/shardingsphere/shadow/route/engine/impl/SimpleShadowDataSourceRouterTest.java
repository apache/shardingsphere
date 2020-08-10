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

package org.apache.shardingsphere.shadow.route.engine.impl;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SimpleShadowDataSourceRouterTest {
    
    private SchemaMetaData schemaMetaData;
    
    private ShadowRule shadowRule;
    
    @Before
    public void setUp() {
        schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "shadow"));
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Collections.singletonMap("ds", "shadow_ds"));
        shadowRule = new ShadowRule(shadowRuleConfiguration);
    }
    
    @Test
    public void judgeForInsert() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0,
                Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("shadow"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        insertStatement.getValues().addAll(Collections.singletonList(new InsertValuesSegment(
                0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "name"), new LiteralExpressionSegment(0, 0, true)))));
        InsertStatementContext insertStatementContext = new InsertStatementContext(schemaMetaData, Collections.emptyList(), insertStatement);
        SimpleShadowDataSourceRouter simpleShadowDataSourceRouter = new SimpleShadowDataSourceRouter(shadowRule, insertStatementContext);
        assertTrue("should be shadow", simpleShadowDataSourceRouter.isShadowSQL());
        insertStatement.getValues().clear();
        insertStatement.getValues().addAll(Collections.singletonList(
                new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "name"), new LiteralExpressionSegment(0, 0, false)))));
        insertStatementContext = new InsertStatementContext(schemaMetaData, Collections.emptyList(), insertStatement);
        simpleShadowDataSourceRouter = new SimpleShadowDataSourceRouter(shadowRule, insertStatementContext);
        assertFalse("should not be shadow", simpleShadowDataSourceRouter.isShadowSQL());
    }
    
    @Test
    public void judgeForWhereSegment() {
        SelectStatement selectStatement = new SelectStatement();
        WhereSegment whereSegment = new WhereSegment(0, 0);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().addAll(Collections.singletonList(
                new PredicateSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("shadow")), new PredicateCompareRightValue(0, 0, "=", new LiteralExpressionSegment(0, 0, true)))));
        whereSegment.getAndPredicates().addAll(Collections.singletonList(andPredicate));
        selectStatement.setWhere(whereSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().addAll(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "true")));
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, Collections.emptyList(), selectStatement);
        SimpleShadowDataSourceRouter simpleShadowDataSourceRouter = new SimpleShadowDataSourceRouter(shadowRule, selectStatementContext);
        assertTrue("should be shadow", simpleShadowDataSourceRouter.isShadowSQL());
        andPredicate.getPredicates().clear();
        andPredicate.getPredicates().addAll(Collections.singletonList(
                new PredicateSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("shadow")), new PredicateCompareRightValue(0, 0, "=", new LiteralExpressionSegment(0, 0, false)))));
        projectionsSegment.getProjections().clear();
        projectionsSegment.getProjections().addAll(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "false")));
        assertFalse("should not be shadow", simpleShadowDataSourceRouter.isShadowSQL());
    }
}
