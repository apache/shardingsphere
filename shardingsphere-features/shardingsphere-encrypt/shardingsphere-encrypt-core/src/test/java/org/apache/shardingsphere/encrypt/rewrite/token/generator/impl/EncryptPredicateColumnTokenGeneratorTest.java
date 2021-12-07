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
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptPredicateColumnTokenGeneratorTest {

    @InjectMocks
    private EncryptPredicateColumnTokenGenerator tokenGenerator;
    
    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertSelectContext context = mock(InsertSelectContext.class);

        when(insertStatementContext.getInsertSelectContext()).thenReturn(context);

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext);
        assertTrue(actual);
    }
    
    @Test
    public void generateSQLTokensWithWhereAvailableTest() {
        Map<String, String> columnTableMap = new HashMap<>();
        columnTableMap.put("qName", "col1");
        IdentifierValue idf = new IdentifierValue("idf");

        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final WhereSegment whereSegment = mock(WhereSegment.class);
        final BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptTable encryptTable = mock(EncryptTable.class);
        final OwnerSegment ownerSegment = mock(OwnerSegment.class);

        when(updateStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));
        when(updateStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.findTableName(anyList(), any())).thenReturn(columnTableMap);
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        when(binaryOperationExpression.getLeft()).thenReturn(columnSegment);
        when(columnSegment.getQualifiedName()).thenReturn("qName");
        when(columnSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        when(columnSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(encryptTable.findEncryptorName(anyString())).thenReturn(Optional.of("encName"));

        tokenGenerator.setSchema(shardingSphereSchema);
        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SubstitutableColumnNameToken> tokens = tokenGenerator.generateSQLTokens(updateStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
    
    @Test
    public void generateSQLTokensWithSelectStatementContextTest() {
        Map<String, String> columnTableMap = new HashMap<>();
        columnTableMap.put("qName", "col1");
        IdentifierValue idf = new IdentifierValue("idf");

        final SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        final SelectStatement selectStatement = mock(SelectStatement.class);
        final BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptTable encryptTable = mock(EncryptTable.class);
        final OwnerSegment ownerSegment = mock(OwnerSegment.class);
        final JoinTableSegment tableSegment = mock(JoinTableSegment.class);

        when(selectStatementContext.isContainsJoinQuery()).thenReturn(true);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.findTableName(anyList(), any())).thenReturn(columnTableMap);
        when(selectStatement.getFrom()).thenReturn(tableSegment);
        when(tableSegment.getCondition()).thenReturn(binaryOperationExpression);
        when(binaryOperationExpression.getLeft()).thenReturn(columnSegment);
        when(columnSegment.getQualifiedName()).thenReturn("qName");
        when(columnSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        when(columnSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(encryptTable.findEncryptorName(anyString())).thenReturn(Optional.of("encName"));
        when(encryptTable.findPlainColumn(anyString())).thenReturn(Optional.of("pCol"));

        tokenGenerator.setSchema(shardingSphereSchema);
        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SubstitutableColumnNameToken> tokens = tokenGenerator.generateSQLTokens(selectStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
}
