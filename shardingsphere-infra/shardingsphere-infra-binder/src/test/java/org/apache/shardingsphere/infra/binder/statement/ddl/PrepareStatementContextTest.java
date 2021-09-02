package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.UnionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.union.UnionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PrepareStatementContextTest {

    private ColumnSegment column;

    private WhereSegment where;

    private SimpleTableSegment table;

    private OwnerSegment owner;

    @Before
    public void setUp() {
        table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        owner = new OwnerSegment(0, 0, new IdentifierValue("owner_1"));
        column = new ColumnSegment(0, 0, new IdentifierValue("col_1"));
        column.setOwner(owner);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, column, null, null, null);
        where = new WhereSegment(0, 0, expression);
    }

    @Test
    public void assertNewInstance() {
        PostgreSQLPrepareStatement postgreSQLPrepare = mock(PostgreSQLPrepareStatement.class);
        when(postgreSQLPrepare.getSelect()).thenReturn(Optional.of(getSelect()));
        when(postgreSQLPrepare.getInsert()).thenReturn(Optional.of(getInsert()));
        when(postgreSQLPrepare.getUpdate()).thenReturn(Optional.of(getUpdate()));
        when(postgreSQLPrepare.getDelete()).thenReturn(Optional.of(getDelete()));
        PrepareStatementContext actual = new PrepareStatementContext(postgreSQLPrepare);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(postgreSQLPrepare));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "owner_1", "tbl_1", "owner_1", "tbl_1", "owner_1", "tbl_1", "owner_1", "tbl_1", "owner_1", "tbl_1", "owner_1", "owner_1", "tbl_1", "owner_1")));
    }

    private SelectStatement getSelect() {
        SelectStatement select = new PostgreSQLSelectStatement();
        SelectStatement unionSelect = new PostgreSQLSelectStatement();
        addSelectConditions(select);
        addSelectConditions(unionSelect);
        select.setUnionSegments(Collections.singletonList(new UnionSegment(UnionType.UNION_ALL, unionSelect, 0, 0)));
        return select;
    }

    private InsertStatement getInsert() {
        InsertStatement insert = new PostgreSQLInsertStatement();
        insert.setTable(table);
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("column_1"));
        column.setOwner(owner);
        ColumnSegment column2 = new ColumnSegment(0, 0, new IdentifierValue("column_2"));
        insert.setInsertColumns(new InsertColumnsSegment(0, 0, Arrays.asList(column, column2)));
        SubquerySegment subQuery = new SubquerySegment(0, 0, getSelect());
        insert.setInsertSelect(subQuery);
        return insert;
    }

    private UpdateStatement getUpdate() {
        UpdateStatement update = new PostgreSQLUpdateStatement();
        update.setTableSegment(table);
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, column, column)));
        update.setSetAssignment(setAssignmentSegment);
        update.setWhere(where);
        return update;
    }

    private DeleteStatement getDelete() {
        DeleteStatement delete = new PostgreSQLDeleteStatement();
        delete.setTableSegment(table);
        delete.setWhere(where);
        return delete;
    }

    private void addSelectConditions(final SelectStatement select) {
        select.setFrom(table);
        select.setWhere(where);
        select.setProjections(new ProjectionsSegment(0, 0));
        OrderByItemSegment orderByItemSegment = mock(OrderByItemSegment.class);
        OrderByItemSegment orderByItemSegment2 = mock(OrderByItemSegment.class);
        Collection<OrderByItemSegment> orderByItems = Arrays.asList(orderByItemSegment, orderByItemSegment2);
        select.setGroupBy(new GroupBySegment(0, 0, orderByItems));
        select.setOrderBy(new OrderBySegment(0, 0, orderByItems));
    }
}
