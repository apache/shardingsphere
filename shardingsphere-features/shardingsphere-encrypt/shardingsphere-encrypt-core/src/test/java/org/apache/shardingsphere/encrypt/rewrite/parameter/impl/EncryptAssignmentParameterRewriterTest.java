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
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptAssignmentParameterRewriterTest {

    @InjectMocks
    private EncryptAssignmentParameterRewriter encryptAssignmentParameterRewriter;

    @Test
    public void isNeedRewriteForEncryptForUpdateContextTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(updateStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForInsertContextTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));

        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(insertStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForDeleteContextTest() {
        final DeleteStatementContext deleteStatementContext = mock(DeleteStatementContext.class);
        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(deleteStatementContext);
        assertFalse(result);
    }

    @Test
    public void rewriteWithUpdateAssignmentSegmentTest() {
        final StandardParameterBuilder standardParameterBuilder = new StandardParameterBuilder(new ArrayList<>());
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final UpdateStatement updateStatement = mock(UpdateStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = mock(ParameterMarkerExpressionSegment.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(updateStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        IdentifierValue identifierValue = new IdentifierValue("table1");
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(updateStatementContext.getSchemaName()).thenReturn("schema1");
        when(updateStatementContext.getSqlStatement()).thenReturn(updateStatement);
        when(updateStatement.getSetAssignment()).thenReturn(setAssignmentSegment);
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getValue()).thenReturn(parameterMarkerExpressionSegment);
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        IdentifierValue iVal = new IdentifierValue("ival", null);
        when(columnSegment.getIdentifier()).thenReturn(iVal);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.getEncryptValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("val1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));
        when(encryptRule.getEncryptAssistedQueryValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));

        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");
        encryptAssignmentParameterRewriter.setEncryptRule(encryptRule);

        encryptAssignmentParameterRewriter.rewrite(standardParameterBuilder, updateStatementContext, parameters);
        assertEquals(1, standardParameterBuilder.getAddedIndexAndParameters().size());
        assertEquals(2, standardParameterBuilder.getAddedIndexAndParameters().get(1).size());
    }

    @Test
    public void rewriteWithInsertAssignmentSegmentTest() {
        final StandardParameterBuilder standardParameterBuilder = new StandardParameterBuilder(new ArrayList<>());
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = mock(ParameterMarkerExpressionSegment.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(insertStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        IdentifierValue identifierValue = new IdentifierValue("table1");
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(insertStatementContext.getSchemaName()).thenReturn("schema1");
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getValue()).thenReturn(parameterMarkerExpressionSegment);
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        IdentifierValue iVal = new IdentifierValue("ival", null);
        when(columnSegment.getIdentifier()).thenReturn(iVal);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.getEncryptValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("val1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));
        when(encryptRule.getEncryptAssistedQueryValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));

        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");
        encryptAssignmentParameterRewriter.setEncryptRule(encryptRule);

        encryptAssignmentParameterRewriter.rewrite(standardParameterBuilder, insertStatementContext, parameters);
        assertEquals(1, standardParameterBuilder.getAddedIndexAndParameters().size());
        assertEquals(2, standardParameterBuilder.getAddedIndexAndParameters().get(1).size());
    }

    @Test
    public void rewriteWithGroupedParameterBuilderTest() {
        List<Object> groupedParameters = new ArrayList<>();
        groupedParameters.add(new Object());
        final GroupedParameterBuilder groupedParameterBuilder = new GroupedParameterBuilder(Collections.singletonList(groupedParameters), new ArrayList<>());
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = mock(ParameterMarkerExpressionSegment.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(insertStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        IdentifierValue identifierValue = new IdentifierValue("table1");
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(insertStatementContext.getSchemaName()).thenReturn("schema1");
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getValue()).thenReturn(parameterMarkerExpressionSegment);
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        IdentifierValue iVal = new IdentifierValue("ival", null);
        when(columnSegment.getIdentifier()).thenReturn(iVal);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.getEncryptValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("val1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));
        when(encryptRule.getEncryptAssistedQueryValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));

        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");
        encryptAssignmentParameterRewriter.setEncryptRule(encryptRule);

        encryptAssignmentParameterRewriter.rewrite(groupedParameterBuilder, insertStatementContext, parameters);
        assertEquals(1, groupedParameterBuilder.getParameterBuilders().size());
        assertEquals(2, groupedParameterBuilder.getParameterBuilders().get(0).getAddedIndexAndParameters().get(1).size());
    }
}
