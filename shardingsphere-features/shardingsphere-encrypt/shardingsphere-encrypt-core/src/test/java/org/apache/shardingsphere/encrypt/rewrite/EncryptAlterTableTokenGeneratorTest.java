package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptAlterTableTokenGeneratorTest {

    @InjectMocks
    private EncryptAlterTableTokenGenerator tokenGenerator;

    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        final AlterTableStatementContext alterTableStatementContext = mock(AlterTableStatementContext.class);

        assertTrue(tokenGenerator.isGenerateSQLTokenForEncrypt(alterTableStatementContext));
    }

    @Test
    public void generateSQLTokensForSQLServerDBTest() {
        final AlterTableStatementContext alterTableStatementContext = getAlterTableStatementContext(new SQLServerDatabaseType());

        final Collection<SQLToken> actualGenerated = tokenGenerator.generateSQLTokens(alterTableStatementContext);

        assertNotNull(actualGenerated);
        assertEquals(8, actualGenerated.size());
    }

    @Test
    public void generateSQLTokensForOracleDBTest() {
        final AlterTableStatementContext alterTableStatementContext = getAlterTableStatementContext(new OracleDatabaseType());

        final Collection<SQLToken> actualGenerated = tokenGenerator.generateSQLTokens(alterTableStatementContext);

        assertNotNull(actualGenerated);
        assertEquals(8, actualGenerated.size());
    }

    @Test
    public void generateSQLTokensForOtherDBTest() {
        final AlterTableStatementContext alterTableStatementContext = getAlterTableStatementContext(new PostgreSQLDatabaseType());

        final Collection<SQLToken> actualGenerated = tokenGenerator.generateSQLTokens(alterTableStatementContext);

        assertNotNull(actualGenerated);
        assertEquals(8, actualGenerated.size());
    }

    private AlterTableStatementContext getAlterTableStatementContext(DatabaseType dbType) {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        final AlterTableStatementContext alterTableStatementContext = mock(AlterTableStatementContext.class);
        final AlterTableStatement alterTableStatement = mock(AlterTableStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final AddColumnDefinitionSegment addColumnDefinitionSegment = mock(AddColumnDefinitionSegment.class);
        final ColumnDefinitionSegment columnDefinitionSegment = mock(ColumnDefinitionSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment = mock(ModifyColumnDefinitionSegment.class);
        final DataTypeSegment dataTypeSegment = mock(DataTypeSegment.class);
        final DropColumnDefinitionSegment dropColumnDefinitionSegment = mock(DropColumnDefinitionSegment.class);


        when(alterTableStatementContext.getSqlStatement()).thenReturn(alterTableStatement);
        when(alterTableStatementContext.getDatabaseType()).thenReturn(dbType);
        when(alterTableStatement.getTable()).thenReturn(simpleTableSegment);
        when(alterTableStatement.getModifyColumnDefinitions()).thenReturn(Collections.singletonList(modifyColumnDefinitionSegment));
        when(modifyColumnDefinitionSegment.getColumnDefinition()).thenReturn(columnDefinitionSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(alterTableStatement.getAddColumnDefinitions()).thenReturn(Collections.singletonList(addColumnDefinitionSegment));
        when(addColumnDefinitionSegment.getColumnDefinitions()).thenReturn(Collections.singletonList(columnDefinitionSegment));
        when(columnDefinitionSegment.getColumnName()).thenReturn(columnSegment);
        when(columnDefinitionSegment.getDataType()).thenReturn(dataTypeSegment);
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptRule.findEncryptor(anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(alterTableStatement.getDropColumnDefinitions()).thenReturn(Collections.singletonList(dropColumnDefinitionSegment));
        when(dropColumnDefinitionSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getQualifiedName()).thenReturn("qColName");

        tokenGenerator.setEncryptRule(encryptRule);
        return alterTableStatementContext;
    }
}