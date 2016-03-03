package com.dangdang.ddframe.rdb.sharding.parser.result;

import java.io.IOException;
import java.util.Arrays;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.Column;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.RouteContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class SQLParsedResultTest {
    
    @Test
    public void assertToString() throws IOException {
        SQLParsedResult actual = new SQLParsedResult();
        generateRouteContext(actual.getRouteContext());
        actual.getConditionContexts().add(generateConditionContext());
        generateMergeContext(actual.getMergeContext());
        assertThat(actual.toString(), is("SQLParsedResult("
                + "routeContext=RouteContext("
                + "tables=[Table(name=order, alias=Optional.of(o)), Table(name=order_item, alias=Optional.absent())], "
                + "sqlStatementType=null, "
                + "sqlBuilder=SELECT * FROM [Token(order)]), "
                + "conditionContexts=[ConditionContext(conditions={Condition.Column(columnName=id, tableName=order)=Condition(column=Condition.Column(columnName=id, tableName=order), "
                + "operator=IN, values=[1, 2, 3])})], "
                + "mergeContext=MergeContext("
                + "orderByColumns=[OrderByColumn(name=Optional.of(id), index=Optional.absent(), alias=Optional.of(a), orderByType=DESC)], "
                + "groupByColumns=[GroupByColumn(name=id, alias=d, orderByType=ASC)], "
                + "aggregationColumns=[AggregationColumn(expression=COUNT(id), aggregationType=COUNT, alias=Optional.of(c), option=Optional.absent(), derivedColumns=[], index=-1)], "
                + "limit=Limit(offset=0, rowCount=10), executorEngine=null))"));
    }
    
    private void generateRouteContext(final RouteContext routeContext) throws IOException {
        routeContext.getTables().add(new Table("order", Optional.of("o")));
        routeContext.getTables().add(new Table("order_item", Optional.<String>absent()));
        routeContext.setSqlBuilder(generateSqlBuilder());
    }
    
    private SQLBuilder generateSqlBuilder() throws IOException {
        SQLBuilder result = new SQLBuilder();
        result.append("SELECT * FROM ");
        result.appendToken("order");
        return result;
    }
    
    private ConditionContext generateConditionContext() {
        ConditionContext result = new ConditionContext();
        Condition condition = new Condition(new Column("id", "order"), BinaryOperator.IN);
        condition.getValues().addAll(Arrays.asList(1, 2, 3));
        result.add(condition);
        return result;
    }
    
    private void generateMergeContext(final MergeContext mergeContext) {
        mergeContext.getAggregationColumns().add(new AggregationColumn("COUNT(id)", AggregationType.COUNT, Optional.of("c"), Optional.<String>absent()));
        mergeContext.getOrderByColumns().add(new OrderByColumn("id", "a", OrderByType.DESC));
        mergeContext.getGroupByColumns().add(new GroupByColumn("id", "d", OrderByType.ASC));
        mergeContext.setLimit(new Limit(0, 10));
    }
}
