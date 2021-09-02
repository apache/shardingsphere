package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateIndexStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateIndexStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        CreateIndexStatementContext actual = assertNewInstance(mock(MySQLCreateIndexStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
        assertThat(actual.getIndexes(), is(Collections.emptyList()));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        PostgreSQLCreateIndexStatement postgreSQLCreateIndexStatement = mock(PostgreSQLCreateIndexStatement.class);
        when(postgreSQLCreateIndexStatement.getGeneratedIndexStartIndex()).thenReturn(java.util.Optional.of(0));
        when(postgreSQLCreateIndexStatement.getColumns()).thenReturn(Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col_1"))));
        CreateIndexStatementContext actual = assertNewInstance(postgreSQLCreateIndexStatement);
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
        assertThat(actual.getIndexes().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("col_1_idx")));
    }

    @Test
    public void assertOracleSQLNewInstance() {
        CreateIndexStatementContext actual = assertNewInstance(mock(OracleCreateIndexStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("Oracle"));
        assertThat(actual.getIndexes(), is(Collections.emptyList()));
    }

    @Test
    public void assertSQLServerSQLNewInstance() {
        CreateIndexStatementContext actual = assertNewInstance(mock(SQLServerCreateIndexStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQLServer"));
        assertThat(actual.getIndexes(), is(Collections.emptyList()));
    }

    private CreateIndexStatementContext assertNewInstance(final CreateIndexStatement createIndexStatement) {
        CreateIndexStatementContext actual = new CreateIndexStatementContext(createIndexStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(createIndexStatement));
        assertTrue(actual.isGeneratedIndex());
        assertThat(actual.getAllTables(), is(Collections.emptyList()));
        when(createIndexStatement.getIndex()).thenReturn(new IndexSegment(0, 0, new IdentifierValue("index_2")));
        CreateIndexStatementContext actual2 = new CreateIndexStatementContext(createIndexStatement);
        assertThat(actual2.getIndexes().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("index_2")));
        when(createIndexStatement.getIndex()).thenReturn(null);
        return actual;
    }
}
