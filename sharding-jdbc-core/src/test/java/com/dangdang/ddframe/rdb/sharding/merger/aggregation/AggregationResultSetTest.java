/**
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

package com.dangdang.ddframe.rdb.sharding.merger.aggregation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetFactory;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.google.common.base.Optional;

public final class AggregationResultSetTest {
    
    @Test
    public void assertNextForSum() throws SQLException {
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                new MockResultSet<Integer>(6), new MockResultSet<Integer>(2), new MockResultSet<Integer>()), createMergeContext(AggregationType.SUM));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(8));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertNextForCount() throws SQLException {
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                new MockResultSet<Integer>(6), new MockResultSet<Integer>(2), new MockResultSet<Integer>()), createMergeContext(AggregationType.COUNT));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(8));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertNextForMax() throws SQLException {
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                new MockResultSet<Integer>(6), new MockResultSet<Integer>(2), new MockResultSet<Integer>()), createMergeContext(AggregationType.MAX));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(6));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertNextForMin() throws SQLException {
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                new MockResultSet<Integer>(6), new MockResultSet<Integer>(2), new MockResultSet<Integer>()), createMergeContext(AggregationType.MIN));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(2));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertNextForAvg() throws SQLException {
        Map<String, Integer> map1 = new LinkedHashMap<>(2);
        map1.put("sharding_gen_1", 5);
        map1.put("sharding_gen_2", 10);
        Map<String, Integer> map2 = new LinkedHashMap<>(2);
        map2.put("sharding_gen_1", 10);
        map2.put("sharding_gen_2", 100);
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                new MockResultSet<Integer>(Arrays.asList(map1)), new MockResultSet<Integer>(Arrays.asList(map2)), new MockResultSet<Integer>()), 
                createMergeContext(AggregationType.AVG, createDerivedColumn(1, AggregationType.COUNT), createDerivedColumn(2, AggregationType.SUM)));
        assertTrue(resultSet.next());
        assertThat(resultSet.getDouble(1), is(7.3333D));
        assertFalse(resultSet.next());
    }
    
    private MergeContext createMergeContext(final AggregationType aggregationType, final AggregationColumn... derivedColumns) {
        AggregationColumn column = new AggregationColumn("column", aggregationType, Optional.<String>absent(), Optional.<String>absent(), 1);
        for (AggregationColumn each : derivedColumns) {
            column.getDerivedColumns().add(each);
        }
        MergeContext result = new MergeContext();
        result.getAggregationColumns().add(column);
        return result;
    }
    
    private AggregationColumn createDerivedColumn(final int index, final AggregationType aggregationType) {
        return new AggregationColumn("column", aggregationType, Optional.of("sharding_gen_" + index), Optional.<String>absent());
    }
}
