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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowInsertValuesTokenGeneratorTest {

    private ShadowInsertValuesTokenGenerator shadowInsertValuesTokenGenerator;

    private InsertStatementContext insertStatementContext;

    @Before
    public void init() {
        String shadowColumn = "shadow_column";
        initShadowInsertValuesTokenGenerator(shadowColumn);
        mockInsertStatementContext(shadowColumn);
    }

    private void mockInsertStatementContext(final String shadowColumn) {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getValues()).thenReturn(createValues());
        insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getInsertColumnNames()).thenReturn(createInsertColumnNames(shadowColumn));
    }

    private Collection<InsertValuesSegment> createValues() {
        List<InsertValuesSegment> values = new LinkedList<>();
        values.add(mock(InsertValuesSegment.class));
        return values;
    }

    private List<String> createInsertColumnNames(final String shadowColumn) {
        List<String> insertColumnNames = new LinkedList<>();
        insertColumnNames.add(shadowColumn);
        insertColumnNames.add("aaa");
        return insertColumnNames;
    }

    private void initShadowInsertValuesTokenGenerator(final String shadowColumn) {
        shadowInsertValuesTokenGenerator = new ShadowInsertValuesTokenGenerator();
        shadowInsertValuesTokenGenerator.setShadowRule(mockShadowRule(shadowColumn));
        shadowInsertValuesTokenGenerator.setPreviousSQLTokens(new LinkedList<>());
    }

    private ShadowRule mockShadowRule(final String shadowColumn) {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getColumn()).thenReturn(shadowColumn);
        return shadowRule;
    }

    @Test
    public void assertIsGenerateSQLTokenForShadow() {
        assertTrue(shadowInsertValuesTokenGenerator.isGenerateSQLTokenForShadow(insertStatementContext));
    }

    @Test
    public void assertGenerateSQLToken() {
        assertNotNull(shadowInsertValuesTokenGenerator.generateSQLToken(insertStatementContext));
    }
}
