package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterIndexStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterIndexStatementContextTest {

    private IndexSegment indexSegment;

    @Before
    public void setUp() {
        indexSegment = new IndexSegment(0, 0,  new IdentifierValue("index_1"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        AlterIndexStatementContext actual = assertNewInstance(mock(PostgreSQLAlterIndexStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.emptyList()));
        assertThat(actual.getIndexes(), is(Arrays.asList(indexSegment)));
    }

    @Test
    public void assertOracleNewInstance() {
        AlterIndexStatementContext actual = assertNewInstance(mock(OracleAlterIndexStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("Oracle"));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.emptyList()));
        assertThat(actual.getIndexes(), is(Collections.singletonList(indexSegment)));
    }

    @Test
    public void assertSQLServerNewInstance() {
        SQLServerAlterIndexStatement alterIndexStatement = mock(SQLServerAlterIndexStatement.class);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        when(alterIndexStatement.getTable()).thenReturn(Optional.of(table));
        AlterIndexStatementContext actual =  assertNewInstance(alterIndexStatement);
        assertThat(actual.getDatabaseType().getName(), is("SQLServer"));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("tbl_1")));
        assertThat(actual.getIndexes(), is(Collections.singletonList(indexSegment)));
    }

    private AlterIndexStatementContext assertNewInstance(final AlterIndexStatement alterIndexStatement) {
        when(alterIndexStatement.getIndex()).thenReturn(Optional.of(indexSegment));
        AlterIndexStatementContext actual = new AlterIndexStatementContext(alterIndexStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(alterIndexStatement));
        return actual;
    }
}
