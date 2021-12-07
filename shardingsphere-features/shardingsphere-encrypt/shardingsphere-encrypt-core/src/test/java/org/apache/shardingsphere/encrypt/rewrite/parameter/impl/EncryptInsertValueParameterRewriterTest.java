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

package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptInsertValueParameterRewriterTest {

    @InjectMocks
    private EncryptInsertValueParameterRewriter reWriter;

    @Test
    public void isNeedRewriteForEncryptForInsertStatementTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement mySQLInsertStatement = mock(MySQLInsertStatement.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(mySQLInsertStatement);
        when(mySQLInsertStatement.getSetAssignment()).thenReturn(Optional.empty());
        when(insertStatementContext.getInsertSelectContext()).thenReturn(null);

        final boolean actual = reWriter.isNeedRewriteForEncrypt(insertStatementContext);
        assertTrue(actual);

    }

    @Test
    public void isNeedRewriteForEncryptForUpdateStatementTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);

        final boolean actual = reWriter.isNeedRewriteForEncrypt(updateStatementContext);
        assertFalse(actual);

    }

    @Test
    public void reWriteWithoutDerivedColsTest() {
        List<Object> groupedParameters = new ArrayList<>();
        groupedParameters.add(new Object());
        GroupedParameterBuilder groupedParameterBuilder = new GroupedParameterBuilder(Collections.singletonList(groupedParameters), new ArrayList<>());
        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");
        IdentifierValue tableNameIdf = new IdentifierValue("table1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final QueryAssistedEncryptAlgorithm encryptAlgorithm = mock(QueryAssistedEncryptAlgorithm.class);
        final InsertValueContext insertValueContext = mock(InsertValueContext.class);
        final ParameterMarkerExpressionSegment expressionSegment = mock(ParameterMarkerExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(tableNameIdf);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatementContext.getDescendingColumnNames()).thenReturn(Collections.singletonList("col1").iterator());
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("assistedCol1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));
        when(encryptAlgorithm.queryAssistedEncrypt(any())).thenReturn("encVal");
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("col1"));
        when(insertStatementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(new Object())));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(insertValueContext.getParameterIndex(anyInt())).thenReturn(0);
        when(insertValueContext.getValueExpressions()).thenReturn(Collections.singletonList(expressionSegment));
        when(insertValueContext.getValue(anyInt())).thenReturn(new Object());

        reWriter.setEncryptRule(encryptRule);

        reWriter.rewrite(groupedParameterBuilder, insertStatementContext, parameters);

        assertEquals(1, groupedParameterBuilder.getParameterBuilders().size());
        assertEquals(1, groupedParameterBuilder.getParameterBuilders().get(0).getAddedIndexAndParameters().size());
        assertEquals(2, groupedParameterBuilder.getParameterBuilders().get(0).getAddedIndexAndParameters().get(1).size());
    }

    @Test
    public void reWriteWithDerivedColsTest() {
        List<Object> groupedParameters = new ArrayList<>();
        groupedParameters.add(new Object());
        GroupedParameterBuilder groupedParameterBuilder = new GroupedParameterBuilder(Collections.singletonList(groupedParameters), new ArrayList<>());
        groupedParameterBuilder.setDerivedColumnName("dCol");
        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");
        IdentifierValue tableNameIdf = new IdentifierValue("table1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final QueryAssistedEncryptAlgorithm encryptAlgorithm = mock(QueryAssistedEncryptAlgorithm.class);
        final InsertValueContext insertValueContext = mock(InsertValueContext.class);
        final ParameterMarkerExpressionSegment expressionSegment = mock(ParameterMarkerExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(tableNameIdf);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatementContext.getDescendingColumnNames()).thenReturn(Collections.singletonList("col1").iterator());
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("assistedCol1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));
        when(encryptAlgorithm.queryAssistedEncrypt(any())).thenReturn("encVal");
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("col1"));
        when(insertStatementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(new Object())));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(insertValueContext.getParameterIndex(anyInt())).thenReturn(0);
        when(insertValueContext.getValueExpressions()).thenReturn(Collections.singletonList(expressionSegment));
        when(insertValueContext.getValueExpressions()).thenReturn(Collections.singletonList(expressionSegment));
        when(insertValueContext.getValue(anyInt())).thenReturn(new Object());

        reWriter.setEncryptRule(encryptRule);

        reWriter.rewrite(groupedParameterBuilder, insertStatementContext, parameters);

        assertEquals(1, groupedParameterBuilder.getParameterBuilders().size());
        assertEquals(1, groupedParameterBuilder.getParameterBuilders().get(0).getAddedIndexAndParameters().size());
        assertEquals(2, groupedParameterBuilder.getParameterBuilders().get(0).getAddedIndexAndParameters().get(1).size());
    }
}
