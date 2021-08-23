package org.apache.shardingsphere.infra.binder.statement.dal;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowColumnsStatementContextTest {

    @Test
    public void assertNewInstance() {
        MySQLShowColumnsStatement mySQLShowColumnsStatement = mock(MySQLShowColumnsStatement.class);
        String tableName = "tbl_1";
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        FromSchemaSegment fromSchema = new FromSchemaSegment(0, 0);
        when(mySQLShowColumnsStatement.getTable()).thenReturn(table);
        when(mySQLShowColumnsStatement.getFromSchema()).thenReturn(Optional.of(fromSchema));
        ShowColumnsStatementContext actual = new ShowColumnsStatementContext(mySQLShowColumnsStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(mySQLShowColumnsStatement));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList(tableName)));
        assertThat(actual.getRemoveSegments(), is(Collections.singletonList(fromSchema)));
    }
}
