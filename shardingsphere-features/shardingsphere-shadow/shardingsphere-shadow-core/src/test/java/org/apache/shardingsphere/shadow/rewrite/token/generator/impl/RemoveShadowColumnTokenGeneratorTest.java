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

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RemoveShadowColumnTokenGeneratorTest {

    private RemoveShadowColumnTokenGenerator removeShadowColumnTokenGenerator;

    private InsertStatementContext insertStatementContext;

    @Before
    public void init() {
        String shadowColumn = "shadow_column";
        initRemoveShadowColumnTokenGenerator(shadowColumn);
        mockInsertStatementContext(shadowColumn);
    }

    private void mockInsertStatementContext(final String shadowColumn) {
        InsertStatement insertStatement = mock(InsertStatement.class);
        InsertColumnsSegment insertColumnsSegment = mockInsertColumnsSegment(shadowColumn);
        insertStatement.setInsertColumns(insertColumnsSegment);
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
    }

    private InsertColumnsSegment mockInsertColumnsSegment(final String shadowColumn) {
        InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);
        when(insertColumnsSegment.getColumns()).thenReturn(initColumns(shadowColumn));
        return insertColumnsSegment;
    }

    private Collection<ColumnSegment> initColumns(final String shadowColumn) {
        Collection<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(1, 6, new IdentifierValue(shadowColumn)));
        columns.add(new ColumnSegment(7, 8, new IdentifierValue("a")));
        return columns;
    }

    private void initRemoveShadowColumnTokenGenerator(final String shadowColumn) {
        removeShadowColumnTokenGenerator = new RemoveShadowColumnTokenGenerator();
        removeShadowColumnTokenGenerator.setShadowRule(mockShadowRule(shadowColumn));
    }

    private ShadowRule mockShadowRule(final String shadowColumn) {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getColumn()).thenReturn(shadowColumn);
        return shadowRule;
    }

    @Test
    public void assertIsGenerateSQLTokenForShadow() {
        assertTrue(removeShadowColumnTokenGenerator.isGenerateSQLTokenForShadow(insertStatementContext));
    }

    @Test
    public void assertGenerateSQLTokens() {
        assertThat(removeShadowColumnTokenGenerator.generateSQLTokens(insertStatementContext).iterator().next().toString(), is(""));
    }
}
