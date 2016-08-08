/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.result;

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

import java.io.IOException;
import java.util.Arrays;

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
                + "operator=IN, values=[1, 2, 3], valueIndices=[])})], "
                + "mergeContext=MergeContext("
                + "orderByColumns=[OrderByColumn(super=AbstractSortableColumn(owner=Optional.absent(), "
                + "name=Optional.of(id), alias=Optional.of(a), orderByType=DESC), index=Optional.absent(), columnIndex=0)], "
                + "groupByColumns=[GroupByColumn(super=AbstractSortableColumn(owner=Optional.absent(), name=Optional.of(id), alias=Optional.of(d), orderByType=ASC), columnIndex=0)], "
                + "aggregationColumns=[AggregationColumn(expression=COUNT(id), aggregationType=COUNT, alias=Optional.of(c), option=Optional.absent(), derivedColumns=[], columnIndex=-1)], "
                + "limit=Limit(offset=0, rowCount=10, offsetParameterIndex=Optional.absent(), rowCountParameterIndex=Optional.absent())))"));
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
        mergeContext.getOrderByColumns().add(new OrderByColumn(Optional.<String>absent(), "id", Optional.of("a"), OrderByType.DESC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "id", Optional.of("d"), OrderByType.ASC));
        mergeContext.setLimit(new Limit(0, 10, Optional.<Integer>absent(), Optional.<Integer>absent()));
    }
}
