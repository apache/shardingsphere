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

package org.apache.shardingsphere.shadow.rewrite.parameter.impl;

import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowUpdateValueParameterRewriterTest {
    
    private ShadowUpdateValueParameterRewriter shadowUpdateValueParameterRewriter;
    
    private UpdateStatementContext updateStatementContext;
    
    @Before
    public void init() {
        String shadowColumn = "shadow_column";
        initShadowUpdateValueParameterRewriter(shadowColumn);
        mockUpdateStatementContext(shadowColumn);
    }
    
    private void mockUpdateStatementContext(final String shadowColumn) {
        UpdateStatement updateStatement = new MySQLUpdateStatement();
        updateStatement.setSetAssignment(createSetAssignmentSegment(shadowColumn));
        updateStatementContext = new UpdateStatementContext(updateStatement, DefaultSchema.LOGIC_NAME);
    }
    
    private SetAssignmentSegment createSetAssignmentSegment(final String shadowColumn) {
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 15, new IdentifierValue(shadowColumn)));
        AssignmentSegment assignment = new ColumnAssignmentSegment(0, 15, columns, mock(ExpressionSegment.class));
        return new SetAssignmentSegment(0, 20, Collections.singletonList(assignment));
    }
    
    private void initShadowUpdateValueParameterRewriter(final String shadowColumn) {
        shadowUpdateValueParameterRewriter = new ShadowUpdateValueParameterRewriter();
        shadowUpdateValueParameterRewriter.setShadowRule(mockShadowRule(shadowColumn));
    }
    
    private ShadowRule mockShadowRule(final String shadowColumn) {
        ShadowRule result = mock(ShadowRule.class);
        when(result.getColumn()).thenReturn(shadowColumn);
        return result;
    }
    
    @Test
    public void assertIsNeedRewriteForShadow() {
        assertTrue(shadowUpdateValueParameterRewriter.isNeedRewriteForShadow(updateStatementContext));
    }
    
    @Test
    public void assertRewrite() {
        shadowUpdateValueParameterRewriter.rewrite(mock(GroupedParameterBuilder.class), updateStatementContext, Collections.emptyList());
    }
}
