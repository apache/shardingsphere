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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptProjectionTokenGeneratorTest {

    @InjectMocks
    private EncryptProjectionTokenGenerator tokenGenerator;
    
    @Test
    public void isGenerateSQLTokenForEncryptNoEmptyTablesTest() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);

        when(selectStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(selectStatementContext);
        assertTrue(actual);
    }
    
    @Test
    public void isGenerateSQLTokenForEncryptWithInsertSelectionContextTest() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        InsertSelectContext insertSelectContext = mock(InsertSelectContext.class);

        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext);
        assertTrue(actual);
    }
    
    @Test
    public void generateSQLTokensWithColumnProjectionsTest() {
        IdentifierValue idf = new IdentifierValue("idf");

        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        InsertSelectContext insertSelectContext = mock(InsertSelectContext.class);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatement selectStatement = mock(SelectStatement.class);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        TablesContext tablesContext = mock(TablesContext.class);
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        ColumnProjectionSegment columnProjectionSegment = mock(ColumnProjectionSegment.class);
        ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(columnProjectionSegment));
        when(columnProjectionSegment.getColumn()).thenReturn(columnSegment);
        when(columnSegment.getIdentifier()).thenReturn(idf);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(Collections.singletonList("table1"));
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(encryptTable.getLogicColumns()).thenReturn(Collections.singletonList("idf"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("pCol"));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SubstitutableColumnNameToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
    
    
    @Test
    public void generateSQLTokensWithShorthandProjectionsTest() {
        IdentifierValue idf = new IdentifierValue("idf");
        DatabaseType databaseType = new MySQLDatabaseType();

        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        InsertSelectContext insertSelectContext = mock(InsertSelectContext.class);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatement selectStatement = mock(SelectStatement.class);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        TablesContext tablesContext = mock(TablesContext.class);
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        ShorthandProjectionSegment shorthandProjectionSegment = mock(ShorthandProjectionSegment.class);
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        ShorthandProjection shorthandProjection = mock(ShorthandProjection.class);
        ColumnProjection columnProjection = mock(ColumnProjection.class);

        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        when(selectStatementContext.getDatabaseType()).thenReturn(databaseType);
        when(projectionsContext.getProjections()).thenReturn(Collections.singletonList(shorthandProjection));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(shorthandProjectionSegment));

        Map<String, ColumnProjection> projectionMap = new HashMap<>();
        projectionMap.put("col1", columnProjection);

        when(shorthandProjection.getActualColumns()).thenReturn(projectionMap);
        when(columnProjection.getName()).thenReturn("col1");
        when(columnProjection.getOwner()).thenReturn("owner");
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(Collections.singletonList("table1"));
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(encryptTable.getLogicColumns()).thenReturn(Collections.singletonList("idf"));

        tokenGenerator.setEncryptRule(encryptRule);
        tokenGenerator.setPreviousSQLTokens(new ArrayList<>());

        final Collection<SubstitutableColumnNameToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
}
