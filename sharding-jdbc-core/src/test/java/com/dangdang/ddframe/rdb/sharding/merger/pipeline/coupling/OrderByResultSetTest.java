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
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrderByResultSetTest {
    
    @Test
    public void assertNextForAsc() throws SQLException {
        ResultSet resultSet = ResultSetFactory.getResultSet(Arrays.<ResultSet>asList(
                new MockResultSet<>(1, 4), new MockResultSet<>(2, 4), new MockResultSet<Integer>()), createSQLContext(OrderType.ASC));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(2));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(4));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(4));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertNextForDesc() throws SQLException {
        ResultSet resultSet = ResultSetFactory.getResultSet(
                Arrays.<ResultSet>asList(new MockResultSet<>(4, 1), new MockResultSet<>(4, 2), new MockResultSet<Integer>()), createSQLContext(OrderType.DESC));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(4));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(4));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(2));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1));
        assertFalse(resultSet.next());
    }
    
    private SQLContext createSQLContext(final OrderType orderType) {
        SQLContext result = new SelectSQLContext();
        result.getOrderByContexts().add(new OrderByContext("name", orderType, Optional.<String>absent()));
        return result;
    }
}
