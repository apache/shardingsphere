package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
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
public class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriterTest {

    @InjectMocks
    private EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter reWriter;

    @Test
    public void isNeedRewriteForEncryptForInsertContextTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement mySQLInsertStatement = mock(MySQLInsertStatement.class);
        final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(mySQLInsertStatement);
        when(mySQLInsertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));

        final boolean result = reWriter.isNeedRewriteForEncrypt(insertStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForUpdateContextTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);

        final boolean result = reWriter.isNeedRewriteForEncrypt(updateStatementContext);
        assertFalse(result);
    }

    @Test
    public void reWriteTest() {
        List<Object> groupedParameters = new ArrayList<>();
        groupedParameters.add(new Object());
        GroupedParameterBuilder groupedParameterBuilder = new GroupedParameterBuilder(Collections.singletonList(groupedParameters), new ArrayList<>());
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final OnDuplicateUpdateContext onDuplicateUpdateContext = mock(OnDuplicateUpdateContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);

        IdentifierValue iVal = new IdentifierValue("ival", null);
        IdentifierValue tableNameIdf = new IdentifierValue("table1");

        when(insertStatementContext.getOnDuplicateKeyUpdateValueContext()).thenReturn(onDuplicateUpdateContext);
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(tableNameIdf);
        when(onDuplicateUpdateContext.getValueExpressions()).thenReturn(Collections.singletonList(expressionSegment));
        when(onDuplicateUpdateContext.getColumn(anyInt())).thenReturn(columnSegment);
        when(onDuplicateUpdateContext.getValue(anyInt())).thenReturn(new Object());
        when(columnSegment.getIdentifier()).thenReturn(iVal);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("val1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));

        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");

        reWriter.setEncryptRule(encryptRule);

        reWriter.rewrite(groupedParameterBuilder, insertStatementContext, parameters);

        assertEquals(1, groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().size());
        assertEquals(1, groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().get(1).size());
    }

    @Test
    public void reWriteWithQueryAssistedEncryptAlgorithmTest() {
        List<Object> groupedParameters = new ArrayList<>();
        groupedParameters.add(new Object());
        GroupedParameterBuilder groupedParameterBuilder = new GroupedParameterBuilder(Collections.singletonList(groupedParameters), new ArrayList<>());
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final OnDuplicateUpdateContext onDuplicateUpdateContext = mock(OnDuplicateUpdateContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final QueryAssistedEncryptAlgorithm encryptAlgorithm = mock(QueryAssistedEncryptAlgorithm.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);

        IdentifierValue iVal = new IdentifierValue("ival", null);
        IdentifierValue tableNameIdf = new IdentifierValue("table1");

        when(insertStatementContext.getOnDuplicateKeyUpdateValueContext()).thenReturn(onDuplicateUpdateContext);
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(tableNameIdf);
        when(onDuplicateUpdateContext.getValueExpressions()).thenReturn(Collections.singletonList(expressionSegment));
        when(onDuplicateUpdateContext.getColumn(anyInt())).thenReturn(columnSegment);
        when(onDuplicateUpdateContext.getValue(anyInt())).thenReturn(new Object());
        when(columnSegment.getIdentifier()).thenReturn(iVal);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("val1"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("val2"));
        when(encryptAlgorithm.queryAssistedEncrypt(any())).thenReturn("p2");

        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");

        reWriter.setEncryptRule(encryptRule);

        reWriter.rewrite(groupedParameterBuilder, insertStatementContext, parameters);

        assertEquals(1, groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().size());
        assertEquals(2, groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().get(1).size());
    }
}