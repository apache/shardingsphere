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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class NullableAggregationResultSetTest {
    
    private final AggregationType aggregationType;
    
    @Parameterized.Parameters(name = "{index}: aggregation type: {0}")
    public static Collection<AggregationType> init() {
        return Arrays.asList(AggregationType.values());
    }
    
    @Test
    public void assertNullable() throws SQLException {
        SQLContext sqlContext = new SelectSQLContext();
        ((SelectSQLContext) sqlContext).getItemContexts().add(MergerTestUtil.createAggregationColumn(aggregationType, aggregationType.name() + "(*)", aggregationType.name(), 1));
        ResultSet resultSet1;
        ResultSet resultSet2;
        if (aggregationType == AggregationType.AVG) {
            resultSet1 = MergerTestUtil.mockResult(Arrays.asList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"));
            resultSet2 = MergerTestUtil.mockResult(Arrays.asList(aggregationType.name(), "sharding_gen_1", "sharding_gen_2"));
        } else {
            resultSet1 = MergerTestUtil.mockResult(Collections.singletonList(aggregationType.name()));
            resultSet2 = MergerTestUtil.mockResult(Collections.singletonList(aggregationType.name()));
        }
        when(resultSet1.next()).thenReturn(true, false);
        when(resultSet2.next()).thenReturn(true, false);
        ResultSet actual = ResultSetFactory.getResultSet(Arrays.asList(resultSet1, resultSet2), sqlContext);
        assertTrue(actual.next());
        assertNull(actual.getObject(1));
        assertThat(actual.getInt(1), is(0));
        assertFalse(actual.next());
    }
}
