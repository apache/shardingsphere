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

package com.dangdang.ddframe.rdb.sharding.merger.common;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.common.fixture.TestDecoratorResultSetMerger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DecoratorResultSetMergerTest {
    
    @Mock
    private ResultSetMerger resultSetMerger;
    
    private TestDecoratorResultSetMerger decoratorResultSetMerger;
    
    @Before
    public void setUp() {
        decoratorResultSetMerger = new TestDecoratorResultSetMerger(resultSetMerger);
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        when(resultSetMerger.getValue(1, Object.class)).thenReturn("1");
        assertThat(decoratorResultSetMerger.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        when(resultSetMerger.getValue("label", Object.class)).thenReturn("1");
        assertThat(decoratorResultSetMerger.getValue("label", Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetCalenderValueWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSetMerger.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat((Date) decoratorResultSetMerger.getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetCalenderValueWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSetMerger.getCalendarValue("label", Date.class, calendar)).thenReturn(new Date(0L));
        assertThat((Date) decoratorResultSetMerger.getCalendarValue("label", Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        when(resultSetMerger.getInputStream(1, "ascii")).thenReturn(null);
        assertNull(decoratorResultSetMerger.getInputStream(1, "ascii"));
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabel() throws SQLException {
        when(resultSetMerger.getInputStream("label", "ascii")).thenReturn(null);
        assertNull(decoratorResultSetMerger.getInputStream("label", "ascii"));
    }
}
