package org.apache.shardingsphere.infra.binder.statement.dal;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLExplainStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExplainStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        ExplainStatementContext actual = assertNewInstance(mock(MySQLExplainStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        ExplainStatementContext actual = assertNewInstance(mock(PostgreSQLExplainStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
    }

    private ExplainStatementContext assertNewInstance(final ExplainStatement explainStatement) {
        SQLStatement statement = () -> 0;
        when(explainStatement.getStatement()).thenReturn(Optional.of(statement));
        ExplainStatementContext actual = new ExplainStatementContext(explainStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(explainStatement));
        assertThat(actual.getSqlStatement().getStatement().orElse(null), is(statement));
        assertThat(actual.getAllTables(), is(Collections.emptyList()));
        return actual;
    }
}
