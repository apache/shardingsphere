package org.apache.shardingsphere.infra.binder.statement.dcl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RevokeStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        RevokeStatementContext actual = assertNewInstance(mock(MySQLRevokeStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        RevokeStatementContext actual = assertNewInstance(mock(PostgreSQLRevokeStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
    }

    @Test
    public void assertOracleNewInstance() {
        RevokeStatementContext actual = assertNewInstance(mock(OracleRevokeStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("Oracle"));
    }

    @Test
    public void assertSQLServerNewInstance() {
        RevokeStatementContext actual = assertNewInstance(mock(SQLServerRevokeStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQLServer"));
    }

    @Test
    public void assertSQL92NewInstance() {
        RevokeStatementContext actual = assertNewInstance(mock(SQL92RevokeStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQL92"));
    }

    private RevokeStatementContext assertNewInstance(final RevokeStatement revokeStatement) {
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        List<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(table1);
        tables.add(table2);
        when(revokeStatement.getTables()).thenReturn(tables);
        RevokeStatementContext actual = new RevokeStatementContext(revokeStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(revokeStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
        return actual;
    }
}
