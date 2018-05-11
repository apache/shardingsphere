/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql.common;

import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.dql.common.fixture.TestDecoratorMergedResult;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DecoratorMergedResultTest {
    
    @Mock
    private MergedResult mergedResult;
    
    private TestDecoratorMergedResult decoratorMergedResult;
    
    @Before
    public void setUp() {
        decoratorMergedResult = new TestDecoratorMergedResult(mergedResult);
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        when(mergedResult.getValue(1, Object.class)).thenReturn("1");
        assertThat(decoratorMergedResult.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        when(mergedResult.getValue("label", Object.class)).thenReturn("1");
        assertThat(decoratorMergedResult.getValue("label", Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetCalenderValueWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat((Date) decoratorMergedResult.getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetCalenderValueWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue("label", Date.class, calendar)).thenReturn(new Date(0L));
        assertThat((Date) decoratorMergedResult.getCalendarValue("label", Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        when(mergedResult.getInputStream(1, "ascii")).thenReturn(null);
        assertNull(decoratorMergedResult.getInputStream(1, "ascii"));
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabel() throws SQLException {
        when(mergedResult.getInputStream("label", "ascii")).thenReturn(null);
        assertNull(decoratorMergedResult.getInputStream("label", "ascii"));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        when(mergedResult.wasNull()).thenReturn(true);
        assertTrue(decoratorMergedResult.wasNull());
    }
}
