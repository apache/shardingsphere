package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.constant.UnionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.union.UnionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterViewStatementContextTest {

    private SimpleTableSegment view;

    @Before
    public void setUp() {
        view = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
    }

    @Test
    public void assertMySQLNewInstance() {
        SelectStatement select = mockMySQLSelectStatement();
        SelectStatement select2 = mockMySQLSelectStatement();
        when(select.getUnionSegments()).thenReturn(Arrays.asList(new UnionSegment(UnionType.UNION_DISTINCT, select2, 0, 0)));
        MySQLAlterViewStatement mySQLAlterViewStatement = mock(MySQLAlterViewStatement.class);
        when(mySQLAlterViewStatement.getSelect()).thenReturn(Optional.of(select));
        AlterViewStatementContext actual = assertNewInstance(mySQLAlterViewStatement);
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
        assertThat(actual.getTablesContext().getTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_2")));
        assertThat(actual.getTablesContext().getOriginalTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_1", "tbl_2", "tbl_2", "tbl_2", "tbl_1", "tbl_2", "tbl_2", "tbl_2")));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        AlterViewStatementContext actual = assertNewInstance(mock(PostgreSQLAlterViewStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
        assertThat(actual.getTablesContext().getTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1")));
        assertThat(actual.getTablesContext().getOriginalTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1")));
    }

    private AlterViewStatementContext assertNewInstance(final AlterViewStatement alterViewStatement) {
        when(alterViewStatement.getView()).thenReturn(view);
        AlterViewStatementContext actual = new AlterViewStatementContext(alterViewStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(alterViewStatement));
        return actual;
    }

    private SelectStatement mockMySQLSelectStatement() {
        MySQLSelectStatement select = mock(MySQLSelectStatement.class);
        when(select.getFrom()).thenReturn(view);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        OwnerSegment owner = new OwnerSegment(0, 0, new IdentifierValue("tbl_2"));
        when(columnSegment.getOwner()).thenReturn(Optional.of(owner));
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, columnSegment, null, null, null);
        when(select.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 0, expression)));
        when(select.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        ColumnSegment columnSegment2 = new ColumnSegment(0, 0, new IdentifierValue("tbl_3"));
        columnSegment2.setOwner(owner);
        when(select.getGroupBy()).thenReturn(Optional.of(new GroupBySegment(0, 0, Arrays.asList(new ColumnOrderByItemSegment(columnSegment2, OrderDirection.ASC)))));
        when(select.getOrderBy()).thenReturn(Optional.of(new OrderBySegment(0, 0, Arrays.asList(new ColumnOrderByItemSegment(columnSegment2, OrderDirection.ASC)))));
        when(select.getLock()).thenReturn(Optional.of(new LockSegment(0, 0)));
        return select;
    }
}
