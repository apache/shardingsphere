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

package com.dangdang.ddframe.rdb.sharding.merger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;

public final class ResultSetUtilTest {
    
    @Test
    public void assertGetValueFromGroupByColumn() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("Name")).thenReturn(null);
        when(resultSet.getObject("NAME")).thenReturn(null);
        when(resultSet.getObject("name")).thenReturn("fromName");
        assertThat(ResultSetUtil.getValue(new GroupByColumn("Name_Column", "Name", OrderByType.ASC), resultSet), is((Object) "fromName"));
        verify(resultSet).getObject("Name");
        verify(resultSet).getObject("NAME");
        verify(resultSet).getObject("name");
    }
    
    @Test(expected = NullPointerException.class)
    public void assertCannotGetValueFromGroupByColumn() throws SQLException {
        ResultSetUtil.getValue(new GroupByColumn("Name_Column", "none", OrderByType.ASC), mock(ResultSet.class));
    }
    
    @Test
    public void assertGetValueFromGroupByColumnForIndex() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn("fromIndex");
        assertThat(ResultSetUtil.getValue(new OrderByColumn(1, OrderByType.ASC), resultSet), is((Object) "fromIndex"));
        verify(resultSet).getObject(1);
    }
    
    @Test
    public void assertGetValueFromGroupByColumnForName() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("Name")).thenReturn(null);
        when(resultSet.getObject("NAME")).thenReturn(null);
        when(resultSet.getObject("name")).thenReturn("fromName");
        assertThat(ResultSetUtil.getValue(new OrderByColumn("Name", OrderByType.ASC), resultSet), is((Object) "fromName"));
        verify(resultSet).getObject("Name");
        verify(resultSet).getObject("NAME");
        verify(resultSet).getObject("name");
    }
    
    @Test(expected = NullPointerException.class)
    public void assertCannotGetValueFromGroupByColumnForIndex() throws SQLException {
        ResultSetUtil.getValue(new OrderByColumn(1, OrderByType.ASC), mock(ResultSet.class));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertCannotGetValueFromGroupByColumnForName() throws SQLException {
        ResultSetUtil.getValue(new OrderByColumn("none", OrderByType.ASC), mock(ResultSet.class));
    }
    
    @Test
    public void assertConvertValueSuccess() {
        assertThat((String) ResultSetUtil.convertValue("1", String.class), is("1"));
        assertThat((int) ResultSetUtil.convertValue(new BigDecimal("1"), int.class), is(1));
        assertThat((long) ResultSetUtil.convertValue(new BigDecimal("1"), long.class), is(1L));
        assertThat((double) ResultSetUtil.convertValue(new BigDecimal("1"), double.class), is(1d));
        assertThat((float) ResultSetUtil.convertValue(new BigDecimal("1"), float.class), is(1f));
        assertThat((BigDecimal) ResultSetUtil.convertValue(new BigDecimal("1"), BigDecimal.class), is(new BigDecimal("1")));
        assertThat((BigDecimal) ResultSetUtil.convertValue((short) 1, BigDecimal.class), is(new BigDecimal("1")));
        assertThat((Date) ResultSetUtil.convertValue(new Date(0L), Date.class), is(new Date(0L)));
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void assertConvertValueFailure() {
        ResultSetUtil.convertValue((short) 1, short.class);
    }
    
    @Test
    public void assertcompareToForAsc() {
        assertTrue(ResultSetUtil.compareTo(1, 2, OrderByType.ASC) < 0);
    }
    
    @Test
    public void assertcompareToForDesc() {
        assertFalse(ResultSetUtil.compareTo(1, 2, OrderByType.DESC) < 0);
    }
}
