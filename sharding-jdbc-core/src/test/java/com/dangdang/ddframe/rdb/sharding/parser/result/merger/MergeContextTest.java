package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext.ResultSetType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.google.common.base.Optional;

public final class MergeContextTest {
    
    @Test
    public void assertGetResultSetTypeWhenGroupBy() {
        MergeContext actual = new MergeContext();
        actual.getGroupByColumns().add(new GroupByColumn("name", "alias", OrderByType.DESC));
        assertThat(actual.getResultSetType(), is(ResultSetType.GroupBy));
    }
    
    @Test
    public void assertGetResultSetTypeWhenAggregate() {
        MergeContext actual = new MergeContext();
        actual.getAggregationColumns().add(new AggregationColumn("COUNT(name)", AggregationType.COUNT, Optional.<String>absent(), Optional.<String>absent()));
        assertThat(actual.getResultSetType(), is(ResultSetType.Aggregate));
    }
    
    @Test
    public void assertGetResultSetTypeWhenOrderBy() {
        MergeContext actual = new MergeContext();
        actual.getOrderByColumns().add(new OrderByColumn(1, OrderByType.DESC));
        assertThat(actual.getResultSetType(), is(ResultSetType.OrderBy));
    }
    
    @Test
    public void assertGetResultSetTypeWhenIterator() {
        MergeContext actual = new MergeContext();
        assertThat(actual.getResultSetType(), is(ResultSetType.Iterator));
    }
}
