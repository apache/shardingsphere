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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GroupByValueTest {
    
    @Mock
    private ResultSet resultSet;
    
    @Before
    public void setUp() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("1");
        when(resultSet.getObject(3)).thenReturn("3");
    }
    
    @Test
    public void assertGetGroupByValues() throws SQLException {
        List<?> actual =  new GroupByValue(resultSet, Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(3, OrderType.DESC))).getGroupValues();
        List<?> expected =  Arrays.asList("1", "3");
        assertTrue(actual.equals(expected));
    }
}
