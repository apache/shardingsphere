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

package org.apache.shardingsphere.shadow.rewrite.judgement.impl;

import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleJudgementEngineTest {

    private RelationMetas relationMetas;

    private ShadowRule shadowRule;

    @Before
    public void setUp() {
        relationMetas = mock(RelationMetas.class);
        when(relationMetas.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "shadow"));
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration();
        shadowRuleConfiguration.setColumn("shadow");
        shadowRule = new ShadowRule(shadowRuleConfiguration);
    }

    @Test
    public void judgeForInsert() {
        InsertStatement insertStatement = new InsertStatement();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0);
        insertColumnsSegment.getColumns().addAll(Arrays.asList(new ColumnSegment(0, 0, "id"),
                new ColumnSegment(0, 0, "name"),
                new ColumnSegment(0, 0, "shadow")));
        insertStatement.setColumns(insertColumnsSegment);
        insertStatement.getValues()
                .addAll(Collections.singletonList(new InsertValuesSegment(0, 0, new ArrayList<ExpressionSegment>() {
                    {
                        add(new LiteralExpressionSegment(0, 0, 1));
                        add(new LiteralExpressionSegment(0, 0, "name"));
                        add(new LiteralExpressionSegment(0, 0, "true"));
                    }
                })));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(relationMetas, Collections.emptyList(), insertStatement);
        SimpleJudgementEngine simpleJudgementEngine = new SimpleJudgementEngine(shadowRule, insertSQLStatementContext);
        Assert.assertTrue("should be shadow", simpleJudgementEngine.isShadowSQL());

        insertStatement.getValues().clear();
        insertStatement.getValues()
                .addAll(Collections.singletonList(new InsertValuesSegment(0, 0, new ArrayList<ExpressionSegment>() {
                    {
                        add(new LiteralExpressionSegment(0, 0, 1));
                        add(new LiteralExpressionSegment(0, 0, "name"));
                        add(new LiteralExpressionSegment(0, 0, "false"));
                    }
                })));
        insertSQLStatementContext = new InsertSQLStatementContext(relationMetas, Collections.emptyList(), insertStatement);
        simpleJudgementEngine = new SimpleJudgementEngine(shadowRule, insertSQLStatementContext);
        Assert.assertFalse("should not be shadow", simpleJudgementEngine.isShadowSQL());
    }

    @Test
    public void judgeForWhereSegment() {
        SelectStatement selectStatement = new SelectStatement();
        WhereSegment whereSegment = new WhereSegment(0, 0);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().addAll(Collections.singletonList(new PredicateSegment(0, 0,
                new ColumnSegment(0, 0, "shadow"),
                new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, "true")))));
        whereSegment.getAndPredicates().addAll(Collections.singletonList(andPredicate));
        selectStatement.setWhere(whereSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections()
                .addAll(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "true")));
        selectStatement.setProjections(projectionsSegment);
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(relationMetas, "", Collections.emptyList(), selectStatement);
        SimpleJudgementEngine simpleJudgementEngine = new SimpleJudgementEngine(shadowRule, selectSQLStatementContext);
        Assert.assertTrue("should be shadow", simpleJudgementEngine.isShadowSQL());

        andPredicate.getPredicates().clear();
        andPredicate.getPredicates().addAll(Collections.singletonList(new PredicateSegment(0, 0,
                new ColumnSegment(0, 0, "shadow"),
                new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, "false")))));
        projectionsSegment.getProjections().clear();
        projectionsSegment.getProjections()
                .addAll(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "false")));
        Assert.assertFalse("should not be shadow", simpleJudgementEngine.isShadowSQL());
    }
}
