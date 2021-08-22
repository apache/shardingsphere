package org.apache.shardingsphere.infra.binder.statement.dal;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLAnalyzeTableStatement;
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

public final class AnalyzeTableStatementContextTest {

    @Test
    public void assertMysqlNewInstance() {
        AnalyzeTableStatementContext actual = assertNewInstance(mock(MySQLAnalyzeTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        AnalyzeTableStatementContext actual = assertNewInstance(mock(PostgreSQLAnalyzeTableStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
    }

    private AnalyzeTableStatementContext assertNewInstance(final AnalyzeTableStatement analyzeTableStatement) {
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        List<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(table1);
        tables.add(table2);
        when(analyzeTableStatement.getTables()).thenReturn(tables);
        AnalyzeTableStatementContext actual = new AnalyzeTableStatementContext(analyzeTableStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(analyzeTableStatement));
        assertThat(actual.getAllTables().stream().map(a -> a.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
        return actual;
    }
}
