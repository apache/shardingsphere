package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTableStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DropTableStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        DropTableStatementContext actual = assertNewInstance(mock(MySQLDropTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        DropTableStatementContext actual = assertNewInstance(mock(PostgreSQLDropTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
    }

    @Test
    public void assertOracleNewInstance() {
        DropTableStatementContext actual = assertNewInstance(mock(OracleDropTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("Oracle"));
    }

    @Test
    public void assertSQLServerNewInstance() {
        DropTableStatementContext actual = assertNewInstance(mock(SQLServerDropTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQLServer"));
    }

    @Test
    public void assertSQL92NewInstance() {
        DropTableStatementContext actual = assertNewInstance(mock(SQL92DropTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQL92"));
    }

    private DropTableStatementContext assertNewInstance(final DropTableStatement dropTableStatement) {
        DropTableStatementContext actual = new DropTableStatementContext(dropTableStatement);
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        when(dropTableStatement.getTables()).thenReturn(Arrays.asList(table1, table2));
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(dropTableStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
        return actual;
    }
}
