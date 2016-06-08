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

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingResultSetsTest {
    
    @Test
    public void assertEmptyShardingResultSets() throws SQLException {
        ShardingResultSets actual = new ShardingResultSets(Arrays.asList(mock(ResultSet.class), mock(ResultSet.class)));
        assertThat(actual.getType(), is(ShardingResultSets.Type.EMPTY));
    }
    
    @Test
    public void assertSingleShardingResultSets() throws SQLException {
        ResultSet resultSet = MergerTestUtil.mockResult(Collections.<String>emptyList());
        when(resultSet.next()).thenReturn(true, false);
        ShardingResultSets actual = new ShardingResultSets(Arrays.asList(resultSet, mock(ResultSet.class)));
        assertThat(actual.getType(), is(ShardingResultSets.Type.SINGLE));
        assertTrue(actual.getResultSets().get(0).next());
        assertFalse(actual.getResultSets().get(0).next());
    }
    
    @Test
    public void assertMultipleShardingResultSets() throws SQLException {
        ResultSet resultSet1 = MergerTestUtil.mockResult(Collections.<String>emptyList());
        when(resultSet1.next()).thenReturn(true, false);
        ResultSet resultSet2 = MergerTestUtil.mockResult(Collections.<String>emptyList());
        when(resultSet2.next()).thenReturn(true, false);
        ShardingResultSets actual = new ShardingResultSets(Arrays.asList(resultSet1, resultSet2));
        assertThat(actual.getType(), is(ShardingResultSets.Type.MULTIPLE));
        assertTrue(actual.getResultSets().get(0).next());
        assertFalse(actual.getResultSets().get(0).next());
        assertTrue(actual.getResultSets().get(1).next());
        assertFalse(actual.getResultSets().get(1).next());
    }
}
