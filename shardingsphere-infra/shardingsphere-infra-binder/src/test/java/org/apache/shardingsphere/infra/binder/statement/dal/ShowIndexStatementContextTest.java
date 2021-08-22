package org.apache.shardingsphere.infra.binder.statement.dal;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShowIndexStatementContextTest {

    @Test
    public void assertNewInstance() {
        MySQLShowIndexStatement mySQLShowIndexStatement = mock(MySQLShowIndexStatement.class);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        when(mySQLShowIndexStatement.getTable()).thenReturn(table);
        ShowIndexStatementContext actual = new ShowIndexStatementContext(mySQLShowIndexStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(mySQLShowIndexStatement));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("tbl_1")));
    }
}
