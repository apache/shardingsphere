package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class CreateDatabaseStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        CreateDatabaseStatementContext actual = assertNewInstance(mock(MySQLCreateDatabaseStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        CreateDatabaseStatementContext actual = assertNewInstance(mock(PostgreSQLCreateDatabaseStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
    }

    private CreateDatabaseStatementContext assertNewInstance(final CreateDatabaseStatement createDatabaseStatement) {
        CreateDatabaseStatementContext actual = new CreateDatabaseStatementContext(createDatabaseStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(createDatabaseStatement));
        assertThat(actual.getTablesContext().getTables(), is(Collections.emptyList()));
        return actual;
    }
}
