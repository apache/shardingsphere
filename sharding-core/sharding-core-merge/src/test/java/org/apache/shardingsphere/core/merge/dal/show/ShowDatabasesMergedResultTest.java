/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.merge.dal.show;

import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowDatabasesMergedResultTest {
    
    private ShowDatabasesMergedResult showDatabasesMergedResult;
    
    @Before
    public void setUp() {
        showDatabasesMergedResult = new ShowDatabasesMergedResult(Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    @Test
    public void assertNext() {
        assertTrue(showDatabasesMergedResult.next());
        assertFalse(showDatabasesMergedResult.next());
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        assertTrue(showDatabasesMergedResult.next());
        assertThat(showDatabasesMergedResult.getValue(1, Object.class).toString(), is(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getInputStream(1, "Ascii");
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(showDatabasesMergedResult.wasNull());
    }

    @Test
    public void assertMergeNext() throws SQLException {
        ShowDatabasesMergedResult actual = buildMergedShowDatabasesMergedResult();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }

    @Test
    public void assertSchemes() throws SQLException {
        ShowDatabasesMergedResult actual = buildMergedShowDatabasesMergedResult();
        actual.next();
        assertThat(actual.getValue(1, String.class).toString(), is("A"));
        actual.next();
        assertThat(actual.getValue(1, String.class).toString(), is("B"));
        actual.next();
        assertThat(actual.getValue(1, String.class).toString(), is("C"));
        actual.next();
        assertThat(actual.getValue(1, String.class).toString(), is("D"));
        actual.next();
        assertThat(actual.getValue(1, String.class).toString(), is("E"));
    }

    private ShowDatabasesMergedResult buildMergedShowDatabasesMergedResult() throws SQLException {
        return new ShowDatabasesMergedResult(Arrays.asList(createQueryResult1(), createQueryResult2()));
    }
    
    private QueryResult createQueryResult1() throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getValue(1, String.class)).thenReturn("A", "B", "C");
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("SCHEMA_NAME");
        return result;
    }
    
    private QueryResult createQueryResult2() throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getValue(1, String.class)).thenReturn("D", "E");
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("SCHEMA_NAME");
        return result;
    }
}
