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
import org.apache.shardingsphere.infra.metadata.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PreparedShadowDataSourceRouterTest {
    
    @Test
    public void isShadowSQL() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "shadow"));
        ShadowRuleConfiguration shadowRuleConfig = new ShadowRuleConfiguration("shadow", Collections.singletonList("ds"), Collections.singletonList("shadow_ds"));
        ShadowRule shadowRule = new ShadowRule(shadowRuleConfig);
        InsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0,
                Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("shadow"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        InsertStatementContext insertStatementContext = new InsertStatementContext(schemaMetaData, Arrays.asList(1, "Tom", 2, "Jerry", 3, true), insertStatement);
        PreparedShadowDataSourceJudgeEngine preparedShadowDataSourceRouter = new PreparedShadowDataSourceJudgeEngine(shadowRule, insertStatementContext, Arrays.asList(1, "Tom", true));
        assertTrue("should be shadow", preparedShadowDataSourceRouter.isShadow());
    }
    
    @Test
    public void isShadowSQLInLiteralExpressionForMySQL() {
        isShadowSQLInLiteralExpression(new MySQLSelectStatement());
    }

    @Test
    public void isShadowSQLInLiteralExpressionForOracle() {
        isShadowSQLInLiteralExpression(new OracleSelectStatement());
    }

    @Test
    public void isShadowSQLInLiteralExpressionForPostgreSQL() {
        isShadowSQLInLiteralExpression(new PostgreSQLSelectStatement());
    }

    @Test
    public void isShadowSQLInLiteralExpressionForSQL92() {
        isShadowSQLInLiteralExpression(new SQL92SelectStatement());
    }

    @Test
    public void isShadowSQLInLiteralExpressionForSQLServer() {
        isShadowSQLInLiteralExpression(new SQLServerSelectStatement());
    }

    private void isShadowSQLInLiteralExpression(final SelectStatement selectStatement) {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "shadow"));
        ShadowRuleConfiguration shadowRuleConfig = new ShadowRuleConfiguration("shadow", Collections.singletonList("ds"), Collections.singletonList("shadow_ds"));
        ShadowRule shadowRule = new ShadowRule(shadowRuleConfig);
        PreparedShadowDataSourceJudgeEngine preparedShadowDataSourceRouter = new PreparedShadowDataSourceJudgeEngine(shadowRule, 
                selectStatementContext(selectStatement), Arrays.asList(1, "Tom", true));
        assertTrue("should be shadow", preparedShadowDataSourceRouter.isShadow());
    }
    
    private SelectStatementContext selectStatementContext(final SelectStatement selectStatement) {
        BinaryOperationExpression left = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("id")), new ParameterMarkerExpressionSegment(0, 0, 0), "=", "id=?");
        BinaryOperationExpression right = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("shadow")), new LiteralExpressionSegment(45, 48, "true"), "=", "shadow=true");
        BinaryOperationExpression binaryOperationExpression = new BinaryOperationExpression(0, 0, left, right, "and", "id=? and shadow=true");
        WhereSegment whereSegment = new WhereSegment(0, 0, binaryOperationExpression);
        selectStatement.setWhere(whereSegment);
        return new SelectStatementContext(selectStatement, null, null, null, null);
    }
}
