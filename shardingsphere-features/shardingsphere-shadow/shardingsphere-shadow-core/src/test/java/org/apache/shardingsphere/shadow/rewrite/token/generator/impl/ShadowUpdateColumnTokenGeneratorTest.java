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

package org.apache.shardingsphere.shadow.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowUpdateColumnTokenGeneratorTest {
    
    private ShadowUpdateColumnTokenGenerator shadowUpdateColumnTokenGenerator;
    
    private UpdateStatementContext updateStatementContext;
    
    @Before
    public void init() {
        String shadowColumn = "shadow_column";
        initShadowUpdateColumnTokenGenerator(shadowColumn);
        mockUpdateStatementContext(shadowColumn);
    }
    
    private void mockUpdateStatementContext(final String shadowColumn) {
        UpdateStatement updateStatement = new MySQLUpdateStatement();
        updateStatement.setSetAssignment(createSetAssignmentSegment(shadowColumn));
        updateStatementContext = new UpdateStatementContext(updateStatement, DefaultSchema.LOGIC_NAME);
    }
    
    private SetAssignmentSegment createSetAssignmentSegment(final String shadowColumn) {
        Collection<AssignmentSegment> assignmentSegments = new LinkedList<>();
        assignmentSegments.add(createAssignmentSegment(0, 15, new IdentifierValue(shadowColumn)));
        assignmentSegments.add(createAssignmentSegment(16, 30, new IdentifierValue("column")));
        return new SetAssignmentSegment(0, 30, assignmentSegments);
    }
    
    private AssignmentSegment createAssignmentSegment(final int startIndex, final int stopIndex, final IdentifierValue identifierValue) {
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(startIndex, stopIndex, identifierValue));
        AssignmentSegment result = new ColumnAssignmentSegment(startIndex, stopIndex, columns, mock(ExpressionSegment.class));
        return result;
    }
    
    private void initShadowUpdateColumnTokenGenerator(final String shadowColumn) {
        shadowUpdateColumnTokenGenerator = new ShadowUpdateColumnTokenGenerator();
        shadowUpdateColumnTokenGenerator.setShadowRule(mockShadowRule(shadowColumn));
    }
    
    private ShadowRule mockShadowRule(final String shadowColumn) {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getColumn()).thenReturn(shadowColumn);
        return shadowRule;
    }
    
    @Test
    public void assertIsGenerateSQLTokenForShadow() {
        assertTrue(shadowUpdateColumnTokenGenerator.isGenerateSQLTokenForShadow(updateStatementContext));
    }
    
    @Test
    public void assertGenerateSQLTokens() {
        assertThat(shadowUpdateColumnTokenGenerator.generateSQLTokens(updateStatementContext).iterator().next().toString(), is(""));
    }
}
