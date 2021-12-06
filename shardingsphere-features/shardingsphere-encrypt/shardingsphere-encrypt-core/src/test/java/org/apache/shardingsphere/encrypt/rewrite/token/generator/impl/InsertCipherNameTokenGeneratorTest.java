package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
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
public class InsertCipherNameTokenGeneratorTest {

    @InjectMocks
    private InsertCipherNameTokenGenerator tokenGenerator;
    
    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        when(insertColumnsSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));

        assertTrue(tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext));
    }
    
    @Test
    public void isGenerateSQLTokenForEncryptForNonInsertTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);

        assertFalse(tokenGenerator.isGenerateSQLTokenForEncrypt(updateStatementContext));
    }


    @Test
    public void generateSQLTokensTest() {
        IdentifierValue idf = new IdentifierValue("idf");
        IdentifierValue idfc = new IdentifierValue("idfc");
        Map<String, String> map = new HashMap<>();
        map.put("idfc", "col1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final SimpleTableSegment tableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        when(insertColumnsSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(insertStatement.getTable()).thenReturn(tableSegment);
        when(tableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.getLogicAndCipherColumns(anyString())).thenReturn(map);
        when(columnSegment.getIdentifier()).thenReturn(idfc);

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SubstitutableColumnNameToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
}