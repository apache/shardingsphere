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

package org.apache.shardingsphere.sql.parser.sql.statement.dml;

import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class InsertStatementTest {
    
    @Test
    public void assertUseDefaultColumns() {
        assertTrue(new InsertStatement().useDefaultColumns());
    }
    
    @Test
    public void assertNotUseDefaultColumnsWithColumns() {
        InsertStatement insertStatement = new InsertStatement();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        assertFalse(insertStatement.useDefaultColumns());
    }
    
    @Test
    public void assertNotUseDefaultColumnsWithSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.emptyList()));
        assertFalse(insertStatement.useDefaultColumns());
    }
    
    @Test
    public void assertGetColumnNamesForInsertColumns() {
        InsertStatement insertStatement = new InsertStatement();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        assertThat(insertStatement.getColumnNames().size(), is(1));
        assertThat(insertStatement.getColumnNames().iterator().next(), is("col"));
    }
    
    @Test
    public void assertGetColumnNamesForSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setSetAssignment(
                new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("col")), new LiteralExpressionSegment(0, 0, 1)))));
        assertThat(insertStatement.getColumnNames().size(), is(1));
        assertThat(insertStatement.getColumnNames().iterator().next(), is("col"));
    }
    
    @Test
    public void assertGetValueListCountWithValues() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 2))));
        assertThat(insertStatement.getValueListCount(), is(2));
    }
    
    @Test
    public void assertGetValueListCountWithSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setSetAssignment(
                new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("col")), new LiteralExpressionSegment(0, 0, 1)))));
        assertThat(insertStatement.getValueListCount(), is(1));
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithValues() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))));
        assertThat(insertStatement.getValueCountForPerGroup(), is(1));
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setSetAssignment(
                new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("col")), new LiteralExpressionSegment(0, 0, 1)))));
        assertThat(insertStatement.getValueCountForPerGroup(), is(1));
    }
    
    @Test
    public void assertGetValueCountForPerGroupWithoutValuesAndSetAssignment() {
        assertThat(new InsertStatement().getValueCountForPerGroup(), is(0));
    }
    
    @Test
    public void assertGetAllValueExpressionsWithValues() {
        InsertStatement insertStatement = new InsertStatement();
        ExpressionSegment valueSegment = new LiteralExpressionSegment(0, 0, 1);
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(valueSegment)));
        assertThat(insertStatement.getAllValueExpressions().size(), is(1));
        assertThat(insertStatement.getAllValueExpressions().iterator().next().size(), is(1));
        assertThat(insertStatement.getAllValueExpressions().iterator().next().iterator().next(), is(valueSegment));
    }
    
    @Test
    public void assertGetAllValueExpressionsWithSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        ExpressionSegment valueSegment = new LiteralExpressionSegment(0, 0, 1);
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("col")), valueSegment))));
        assertThat(insertStatement.getAllValueExpressions().size(), is(1));
        assertThat(insertStatement.getAllValueExpressions().iterator().next().size(), is(1));
        assertThat(insertStatement.getAllValueExpressions().iterator().next().iterator().next(), is(valueSegment));
    }
}
