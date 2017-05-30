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

package com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetFactory;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.TestResultSetRow;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.ResultSetRow;
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class AggregationResultSetTest {
    
    private final AggregationType aggregationType;
    
    private final List<String> columnNames;
    
    private final Object[] resultSetData1;
    
    private final Object[] resultSetData2;
    
    private final Number result;
    
    @Parameterized.Parameters(name = "{index}: aggregation type: {0}, column names: {1}, result set data 2: {2}, result set data 2: {3}, result: {4}")
    public static Collection init() {
        return Arrays.asList(new Object[][] {
                {AggregationType.SUM, Collections.singletonList("SUM(0)"), new Object[] {6}, new Object[] {2}, 8},
                {AggregationType.COUNT, Collections.singletonList("COUNT(`id`)"), new Object[] {6}, new Object[] {2}, 8},
                {AggregationType.MAX, Collections.singletonList("MAX_RESULT"), new Object[] {6}, new Object[] {2}, 6},
                {AggregationType.MIN, Collections.singletonList("MIN_RESULT"), new Object[] {6}, new Object[] {2}, 2},
                {AggregationType.AVG, Arrays.asList("AVG(*)", "sharding_gen_1", "sharding_gen_2"), new Object[] {2, 5, 10}, new Object[] {10, 10, 100}, 7.3333D},
        });
    }
    
    @Test
    public void assertNext() throws SQLException {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getItems().add(MergerTestUtil.createAggregationColumn(aggregationType, columnNames.get(0), null, 1));
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.asList(
                MergerTestUtil.mockResult(columnNames, Collections.<ResultSetRow>singletonList(new TestResultSetRow(resultSetData1))),
                MergerTestUtil.mockResult(columnNames, Collections.<ResultSetRow>singletonList(new TestResultSetRow(resultSetData2))),
                MergerTestUtil.mockResult(Collections.<String>emptyList())), selectStatement);
        assertTrue(resultSet.next());
        if (AggregationType.AVG == aggregationType) {
            assertThat(resultSet.getDouble(1), is(result));
            assertThat(resultSet.getDouble(columnNames.get(0)), is(result));
        } else {
            assertThat(resultSet.getInt(1), is(result));
            assertThat(resultSet.getInt(columnNames.get(0)), is(result));
        }
        assertFalse(resultSet.next());
    }
}
