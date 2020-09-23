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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InsertStatementHandlerTest {
    
    @Test
    public void assertGetOnDuplicateKeyColumnsSegmentWithOnDuplicateKeyColumnsSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList()));
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertTrue(onDuplicateKeyColumnsSegment.isPresent());
    }
    
    @Test
    public void assertGetOnDuplicateKeyColumnsSegmentWithoutOnDuplicateKeyColumnsSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertFalse(onDuplicateKeyColumnsSegment.isPresent());
    }
    
    @Test
    public void assertGetSetAssignmentSegmentWithSetAssignmentSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        assertTrue(setAssignmentSegment.isPresent());
    }
    
    @Test
    public void assertGetSetAssignmentSegmentWithoutSetAssignmentSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        assertFalse(setAssignmentSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithWithSegmentForPostgreSQL() {
        PostgreSQLInsertStatement insertStatement = new PostgreSQLInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithoutWithSegmentForPostgreSQL() {
        PostgreSQLInsertStatement insertStatement = new PostgreSQLInsertStatement();
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertFalse(withSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithWithSegmentForSQLServer() {
        SQLServerInsertStatement insertStatement = new SQLServerInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithoutWithSegmentForSQLServer() {
        SQLServerInsertStatement insertStatement = new SQLServerInsertStatement();
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertFalse(withSegment.isPresent());
    }
    
    @Test
    public void assertGetColumnNamesForInsertColumnsForMySQL() {
        assertGetColumnNamesForInsertColumns(new MySQLInsertStatement());
    }
    
    @Test
    public void assertGetColumnNamesForInsertColumnsForOracle() {
        assertGetColumnNamesForInsertColumns(new OracleInsertStatement());
    }
    
    @Test
    public void assertGetColumnNamesForInsertColumnsForPostgreSQL() {
        assertGetColumnNamesForInsertColumns(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertGetColumnNamesForInsertColumnsForSQL92() {
        assertGetColumnNamesForInsertColumns(new SQL92InsertStatement());
    }
    
    @Test
    public void assertGetColumnNamesForInsertColumnsForSQLServer() {
        assertGetColumnNamesForInsertColumns(new SQLServerInsertStatement());
    }
    
    private void assertGetColumnNamesForInsertColumns(final InsertStatement insertStatement) {
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        List<String> columnNames = InsertStatementHandler.getColumnNames(insertStatement);
        assertThat(columnNames.size(), is(1));
        assertThat(columnNames.iterator().next(), is("col"));
    }
    
    @Test
    public void assertGetColumnNamesForSetAssignmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, 
                new ColumnSegment(0, 0, new IdentifierValue("col")), new LiteralExpressionSegment(0, 0, 1)))));
        List<String> columnNames = InsertStatementHandler.getColumnNames(insertStatement);
        assertThat(columnNames.size(), is(1));
        assertThat(columnNames.iterator().next(), is("col"));
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithValuesForMySQL() {
        assertGetValueCountForPerGroupWithValues(new MySQLInsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithValuesForOracle() {
        assertGetValueCountForPerGroupWithValues(new OracleInsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithValuesForPostgreSQL() {
        assertGetValueCountForPerGroupWithValues(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithValuesForSQL92() {
        assertGetValueCountForPerGroupWithValues(new SQL92InsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithValuesForSQLServer() {
        assertGetValueCountForPerGroupWithValues(new SQLServerInsertStatement());
    }
    
    private void assertGetValueCountForPerGroupWithValues(final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))));
        assertThat(InsertStatementHandler.getValueCountForPerGroup(insertStatement), is(1));
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithSetAssignmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0,
                new ColumnSegment(0, 0, new IdentifierValue("col")), new LiteralExpressionSegment(0, 0, 1)))));
        assertThat(InsertStatementHandler.getValueCountForPerGroup(insertStatement), is(1));
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithoutValuesAndSetAssignmentForMySQL() {
        assertGetValueCountForPerGroupWithoutValuesAndSetAssignment(new MySQLInsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithoutValuesAndSetAssignmentForOracle() {
        assertGetValueCountForPerGroupWithoutValuesAndSetAssignment(new OracleInsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithoutValuesAndSetAssignmentForPostgreSQL() {
        assertGetValueCountForPerGroupWithoutValuesAndSetAssignment(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithoutValuesAndSetAssignmentForSQL92() {
        assertGetValueCountForPerGroupWithoutValuesAndSetAssignment(new SQL92InsertStatement());
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithoutValuesAndSetAssignmentForSQLServer() {
        assertGetValueCountForPerGroupWithoutValuesAndSetAssignment(new SQLServerInsertStatement());
    }
    
    private void assertGetValueCountForPerGroupWithoutValuesAndSetAssignment(final InsertStatement insertStatement) {
        assertThat(InsertStatementHandler.getValueCountForPerGroup(insertStatement), is(0));
    }
    
    @Test
    public void assertGetAllValueExpressionsWithValuesForMySQL() {
        assertGetAllValueExpressionsWithValues(new MySQLInsertStatement());
    }
    
    @Test
    public void assertGetAllValueExpressionsWithValuesForOracle() {
        assertGetAllValueExpressionsWithValues(new OracleInsertStatement());
    }
    
    @Test
    public void assertGetAllValueExpressionsWithValuesForPostgreSQL() {
        assertGetAllValueExpressionsWithValues(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertGetAllValueExpressionsWithValuesForSQL92() {
        assertGetAllValueExpressionsWithValues(new SQL92InsertStatement());
    }
    
    @Test
    public void assertGetAllValueExpressionsWithValuesForSQLServer() {
        assertGetAllValueExpressionsWithValues(new SQLServerInsertStatement());
    }
    
    private void assertGetAllValueExpressionsWithValues(final InsertStatement insertStatement) {
        ExpressionSegment valueSegment = new LiteralExpressionSegment(0, 0, 1);
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(valueSegment)));
        List<List<ExpressionSegment>> allValueExpressions = InsertStatementHandler.getAllValueExpressions(insertStatement);
        assertThat(allValueExpressions.size(), is(1));
        assertThat(allValueExpressions.iterator().next().size(), is(1));
        assertThat(allValueExpressions.iterator().next().iterator().next(), is(valueSegment));
    }
    
    @Test
    public void assertGetAllValueExpressionsWithSetAssignmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        ExpressionSegment valueSegment = new LiteralExpressionSegment(0, 0, 1);
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("col")), valueSegment))));
        List<List<ExpressionSegment>> allValueExpressions = InsertStatementHandler.getAllValueExpressions(insertStatement);
        assertThat(allValueExpressions.size(), is(1));
        assertThat(allValueExpressions.iterator().next().size(), is(1));
        assertThat(allValueExpressions.iterator().next().iterator().next(), is(valueSegment));
    }
}
