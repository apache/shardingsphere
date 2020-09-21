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

package org.apache.shardingsphere.shadow.route.engine.judge.impl;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
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
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Collections.singletonList("ds"), Collections.singletonList("shadow_ds"));
        shadowRule = new ShadowRule(shadowRuleConfiguration);
    }
    
    @Test
    public void judgeForMySQLInsert() {
        judgeForInsert(new MySQLInsertStatement());
    }
    
    @Test
    public void judgeForOracleInsert() {
        judgeForInsert(new OracleInsertStatement());
    }
    
    @Test
    public void judgeForPostgreSQLInsert() {
        judgeForInsert(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void judgeForSQL92Insert() {
        judgeForInsert(new SQL92InsertStatement());
    }
    
    @Test
    public void judgeForSQLServerInsert() {
        judgeForInsert(new SQLServerInsertStatement());
    }
    
    private void judgeForInsert(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0,
                Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("shadow"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        insertStatement.getValues().addAll(Collections.singletonList(new InsertValuesSegment(
                0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "name"), new LiteralExpressionSegment(0, 0, true)))));
        InsertStatementContext insertStatementContext = new InsertStatementContext(schemaMetaData, Collections.emptyList(), insertStatement);
        SimpleShadowDataSourceJudgeEngine simpleShadowDataSourceRouter = new SimpleShadowDataSourceJudgeEngine(shadowRule, insertStatementContext);
        assertTrue("should be shadow", simpleShadowDataSourceRouter.isShadow());
        insertStatement.getValues().clear();
        insertStatement.getValues().addAll(Collections.singletonList(
                new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "name"), new LiteralExpressionSegment(0, 0, false)))));
        insertStatementContext = new InsertStatementContext(schemaMetaData, Collections.emptyList(), insertStatement);
        simpleShadowDataSourceRouter = new SimpleShadowDataSourceJudgeEngine(shadowRule, insertStatementContext);
        assertFalse("should not be shadow", simpleShadowDataSourceRouter.isShadow());
    }
    
    @Test
    public void judgeForWhereSegment() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("shadow"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, true);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "=", null);
        WhereSegment whereSegment = new WhereSegment(0, 0, expression);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setWhere(whereSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().addAll(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "true")));
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, Collections.emptyList(), selectStatement);
        SimpleShadowDataSourceJudgeEngine simpleShadowDataSourceRouter = new SimpleShadowDataSourceJudgeEngine(shadowRule, selectStatementContext);
        assertTrue("should be shadow", simpleShadowDataSourceRouter.isShadow());
        expression = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("shadow")), new LiteralExpressionSegment(0, 0, false), "=", null);
        whereSegment = new WhereSegment(0, 0, expression);
        selectStatement.setWhere(whereSegment);
        projectionsSegment.getProjections().clear();
        projectionsSegment.getProjections().addAll(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "false")));
        assertFalse("should not be shadow", simpleShadowDataSourceRouter.isShadow());
    }
}
