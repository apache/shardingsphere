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

package org.apache.shardingsphere.core.parse.sql.statement.dml;

import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
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
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "col"));
        assertFalse(insertStatement.useDefaultColumns());
    }
    
    @Test
    public void assertNotUseDefaultColumnsWithSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentsSegment(0, 0, Collections.<AssignmentSegment>emptyList()));
        assertFalse(insertStatement.useDefaultColumns());
    }
    
    @Test
    public void assertGetValueSizeWithValues() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(0, 0, 1))));
        assertThat(insertStatement.getValueSize(), is(1));
    }
    
    @Test
    public void assertGetValueSizeWithSetAssignment() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setSetAssignment(
                new SetAssignmentsSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col"), new LiteralExpressionSegment(0, 0, 1)))));
        assertThat(insertStatement.getValueSize(), is(1));
    }
    
    @Test
    public void assertGetValueSizeWithoutValuesAndSetAssignment() {
        assertThat(new InsertStatement().getValueSize(), is(0));
    }
}
