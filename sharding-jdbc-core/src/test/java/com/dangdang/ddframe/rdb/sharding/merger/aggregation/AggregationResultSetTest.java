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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetFactory;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class AggregationResultSetTest {
    
    private final TestTarget type;
    
    private final AggregationType aggregationType;
    
    private final List<String> columns;
    
    private final List<Integer> resultSet1;
    
    private final List<Integer> resultSet2;
    
    private final Optional<String> nameOfGetResult;
    
    private final Class<? extends Number> resultClass;
    
    private final Number result;
    
    @Parameterized.Parameters(name = "{index}: testTarget:{0}, aggregation type:{1}, columns:{2}, r1:{3}, r2:{4}, rsName:{5}, rsClass:{6}, result:{7}")
    public static Collection init() {
        
        return Arrays.asList(new Object[][]{
                {TestTarget.INDEX, AggregationType.SUM, Collections.singletonList(""), Collections.singletonList(6), Collections.singletonList(2), Optional.absent(), Integer.class, 8}, 
                {TestTarget.COLUMN_NAME, AggregationType.SUM, Collections.singletonList("SUM(0)"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("SUM(0)"), 
                    Integer.class, 8}, 
                {TestTarget.ALIAS, AggregationType.SUM, Collections.singletonList("SUM_RESULT"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("SUM_RESULT"), 
                    Integer.class, 8}, 
                {TestTarget.INDEX, AggregationType.COUNT, Collections.singletonList(""), Collections.singletonList(6), Collections.singletonList(2), Optional.absent(), Integer.class, 8},
                {TestTarget.COLUMN_NAME, AggregationType.COUNT, Collections.singletonList("COUNT(`id`)"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("COUNT(`id`)"),
                    Integer.class, 8}, 
                {TestTarget.ALIAS, AggregationType.COUNT, Collections.singletonList("COUNT_RESULT"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("COUNT_RESULT"), 
                    Integer.class, 8}, 
                {TestTarget.INDEX, AggregationType.MAX, Collections.singletonList(""), Collections.singletonList(6), Collections.singletonList(2), Optional.absent(), Integer.class, 6},
                {TestTarget.COLUMN_NAME, AggregationType.MAX, Collections.singletonList("MAX(id)"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("MAX(`id`)"), 
                    Integer.class, 6}, 
                {TestTarget.ALIAS, AggregationType.MAX, Collections.singletonList("MAX_RESULT"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("MAX_RESULT"), 
                    Integer.class, 6}, 
                {TestTarget.INDEX, AggregationType.MIN, Collections.singletonList(""), Collections.singletonList(6), Collections.singletonList(2), Optional.absent(), Integer.class, 2},
                {TestTarget.COLUMN_NAME, AggregationType.MIN, Collections.singletonList("MIN(0)"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("MIN(0)"), 
                    Integer.class, 2}, 
                {TestTarget.ALIAS, AggregationType.MIN, Collections.singletonList("MIN_RESULT"), Collections.singletonList(6), Collections.singletonList(2), Optional.of("MIN_RESULT"), 
                    Integer.class, 2}, 
                {TestTarget.INDEX, AggregationType.AVG, Arrays.asList("sharding_gen_1", "sharding_gen_2"), Arrays.asList(5, 10), Arrays.asList(10, 100), Optional.absent(), Double.class, 7.3333D},
                {TestTarget.COLUMN_NAME, AggregationType.AVG, Arrays.asList("sharding_gen_1", "sharding_gen_2"), Arrays.asList(5, 10), Arrays.asList(10, 100), Optional.of("AVG(*)"), 
                    Double.class, 7.3333D}, 
                {TestTarget.ALIAS, AggregationType.AVG, Arrays.asList("sharding_gen_1", "sharding_gen_2"), Arrays.asList(5, 10), Arrays.asList(10, 100), Optional.of("AVG_RESULT"), 
                    Double.class, 7.3333D}
        });
        
    }
    
    @Test
    public void assertNext() throws SQLException {
        MergeContext mergeContext;
        switch (type) {
            case INDEX:
                mergeContext = MergerTestUtil.createMergeContext(1, "column", null, aggregationType);
                break;
            case COLUMN_NAME:
                mergeContext = MergerTestUtil.createMergeContext(1, nameOfGetResult.get(), null, aggregationType);
                break;
            case ALIAS:
                mergeContext = MergerTestUtil.createMergeContext(1, "column", nameOfGetResult.get(), aggregationType);
                break;
            default:
                throw new RuntimeException();
        }
        
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                MergerTestUtil.createMock(columns, resultSet1), MergerTestUtil.createMock(columns, resultSet2), new MockResultSet<Integer>()),
                mergeContext);
        assertTrue(resultSet.next());
    
        Number actual;
        switch (type) {
            case INDEX:
                if (Integer.class.equals(resultClass)) {
                    actual = resultSet.getInt(1);
                } else if (Double.class.equals(resultClass)) {
                    actual = resultSet.getDouble(1);
                } else {
                    throw new RuntimeException();
                }
                break;
            default:
                if (Integer.class.equals(resultClass)) {
                    actual = resultSet.getInt(this.nameOfGetResult.get());
                } else if (Double.class.equals(resultClass)) {
                    actual = resultSet.getDouble(this.nameOfGetResult.get());
                } else {
                    throw new RuntimeException();
                }
                break;
        }
        assertThat(actual, is(result));
        assertFalse(resultSet.next());
    }
    
    private enum TestTarget {
        INDEX, COLUMN_NAME, ALIAS
    }
}
