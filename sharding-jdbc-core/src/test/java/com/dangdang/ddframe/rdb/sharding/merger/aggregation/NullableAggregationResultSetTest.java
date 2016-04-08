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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetFactory;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class NullableAggregationResultSetTest {
    
    private final AggregationColumn.AggregationType aggregationType;
    
    @Parameterized.Parameters(name = "{index}: aggregation type:{0}")
    public static Collection init() {
        return Arrays.asList(AggregationColumn.AggregationType.values());
    }
    
    @Test
    public void testNullable() throws SQLException {
        MergeContext result = MergerTestUtil.createMergeContext(1, aggregationType.name() + "(*)", aggregationType.name(), aggregationType);
        List<ResultSet> resultSetList = new ArrayList<>();
        if (aggregationType.equals(AggregationColumn.AggregationType.AVG)) {
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"), Lists.newArrayList(new Integer[]{null, null, null})));
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"), Lists.newArrayList(new Integer[]{null, null, null})));
        } else {
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name()), Lists.newArrayList(new Integer[]{null})));
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name()), Lists.newArrayList(new Integer[]{null})));
        }
        ResultSet resultSet = ResultSetFactory.getResultSet(resultSetList, result);
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getObject(1), nullValue());
        assertThat(resultSet.getInt(1), is(0));
        assertThat(resultSet.next(), is(false));
    }
    
    @Test
    public void testMix() throws SQLException {
        MergeContext result = MergerTestUtil.createMergeContext(1, aggregationType.name() + "(*)", aggregationType.name(), aggregationType);
        
        List<ResultSet> resultSetList = new ArrayList<>();
        if (aggregationType.equals(AggregationColumn.AggregationType.AVG)) {
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"), Lists.newArrayList(new Integer[]{null, null, null})));
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"), Lists.newArrayList(null, 1, 1)));
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"), Lists.newArrayList(new Integer[]{null, null, null})));
        } else {
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name()), Lists.newArrayList(new Integer[]{null})));
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name()), Lists.newArrayList(1)));
            resultSetList.add(MergerTestUtil.createMock(Lists.newArrayList(aggregationType.name()), Lists.newArrayList(new Integer[]{null})));
        }
        ResultSet resultSet = ResultSetFactory.getResultSet(resultSetList, result);
        assertThat(resultSet.next(), is(true));
        if (AggregationColumn.AggregationType.SUM.equals(aggregationType) || AggregationColumn.AggregationType.COUNT.equals(aggregationType)) {
            assertThat(resultSet.getObject(1), Is.<Object>is(new BigDecimal(1)));
        } else if (AggregationColumn.AggregationType.MAX.equals(aggregationType) || AggregationColumn.AggregationType.MIN.equals(aggregationType)) {
            assertThat(resultSet.getObject(1), Is.<Object>is(1));
        } else if (AggregationColumn.AggregationType.AVG.equals(aggregationType)) {
            assertThat(resultSet.getObject(1), Is.<Object>is(new BigDecimal(1).divide(new BigDecimal(1), 4, BigDecimal.ROUND_HALF_UP)));
        }
        
        assertThat(resultSet.next(), is(false));
    }
}
