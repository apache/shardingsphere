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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Before;
import org.junit.Test;

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
        updateStatementContext = new UpdateStatementContext(updateStatement);
    }
    
    private SetAssignmentSegment createSetAssignmentSegment(final String shadowColumn) {
        return new SetAssignmentSegment(0, 20, Lists.newArrayList(new AssignmentSegment(0, 15, new ColumnSegment(0, 15, new IdentifierValue(shadowColumn)), mock(ExpressionSegment.class))));
    }
    
    private void initShadowUpdateValueParameterRewriter(final String shadowColumn) {
        shadowUpdateValueParameterRewriter = new ShadowUpdateValueParameterRewriter();
        shadowUpdateValueParameterRewriter.setShadowRule(mockShadowRule(shadowColumn));
    }
    
    private ShadowRule mockShadowRule(final String shadowColumn) {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getColumn()).thenReturn(shadowColumn);
        return shadowRule;
    }
    
    @Test
    public void assertIsNeedRewriteForShadow() {
        assertTrue(shadowUpdateValueParameterRewriter.isNeedRewriteForShadow(updateStatementContext));
    }
    
    @Test
    public void assertRewrite() {
        shadowUpdateValueParameterRewriter.rewrite(mock(GroupedParameterBuilder.class), updateStatementContext, mock(List.class));
    }
}
