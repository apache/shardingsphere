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

package org.apache.shardingsphere.sql.parser.engine.hive.visitor.statement;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiveStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitTransformOutputVariables() {
        SelectStatement actual = parseSelectStatement("SELECT TRANSFORM(user_id) USING 'cat' AS (transformed_user_id INT) FROM t_user");
        assertTrue(actual.getVariableNames().contains("transformed_user_id"));
    }
    
    @Test
    void assertVisitTransformDefaultOutputVariables() {
        SelectStatement actual = parseSelectStatement("SELECT TRANSFORM(user_id) USING 'cat' FROM t_user");
        assertTrue(actual.getVariableNames().contains("key"));
        assertTrue(actual.getVariableNames().contains("value"));
    }
    
    @Test
    void assertVisitHiveSystemColumnVariables() {
        SelectStatement actual = parseSelectStatement("SELECT `INPUT__FILE__NAME` FROM t_user");
        assertTrue(actual.getVariableNames().contains("INPUT__FILE__NAME"));
    }
    
    @Test
    void assertVisitUpdateCastAssignment() {
        UpdateStatement actual = parseUpdateStatement("UPDATE t_product_detail SET description = cast(? AS STRING) WHERE detail_id = ?");
        ColumnAssignmentSegment actualAssignment = actual.getAssignment().get().getAssignments().iterator().next();
        assertTrue(FunctionSegment.class.isInstance(actualAssignment.getValue()));
        FunctionSegment actualValue = (FunctionSegment) actualAssignment.getValue();
        assertThat(actualValue.getFunctionName(), is("cast"));
    }
    
    @Test
    void assertVisitUpdateCaseWhenAssignment() {
        UpdateStatement actual = parseUpdateStatement("UPDATE t_user SET user_name = CASE WHEN user_id = ? THEN concat(user_name, ?) ELSE user_name END WHERE user_id IN (?, ?)");
        ColumnAssignmentSegment actualAssignment = actual.getAssignment().get().getAssignments().iterator().next();
        assertTrue(CaseWhenExpression.class.isInstance(actualAssignment.getValue()));
        CaseWhenExpression actualValue = (CaseWhenExpression) actualAssignment.getValue();
        assertThat(actualValue.getText(), is("CASE WHEN user_id = ? THEN concat(user_name, ?) ELSE user_name END"));
    }
    
    private SelectStatement parseSelectStatement(final String sql) {
        ParseASTNode parseASTNode = new SQLParserEngine("Hive", CACHE_OPTION).parse(sql, false);
        return (SelectStatement) new SQLStatementVisitorEngine("Hive").visit(parseASTNode);
    }
    
    private UpdateStatement parseUpdateStatement(final String sql) {
        ParseASTNode parseASTNode = new SQLParserEngine("Hive", CACHE_OPTION).parse(sql, false);
        return (UpdateStatement) new SQLStatementVisitorEngine("Hive").visit(parseASTNode);
    }
}
