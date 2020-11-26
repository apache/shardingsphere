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

package org.apache.shardingsphere.infra.merge.result.impl.transparent;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TransparentMergedResultTest {
    
    @Test
    public void assertNext() throws SQLException {
        QueryResultSet queryResultSet = mock(QueryResultSet.class);
        when(queryResultSet.next()).thenReturn(true, false);
        TransparentMergedResult actual = new TransparentMergedResult(queryResultSet);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValue() throws SQLException {
        QueryResultSet queryResultSet = mock(QueryResultSet.class);
        when(queryResultSet.getValue(1, Object.class)).thenReturn("1");
        TransparentMergedResult actual = new TransparentMergedResult(queryResultSet);
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetCalendarValue() throws SQLException {
        QueryResultSet queryResultSet = mock(QueryResultSet.class);
        when(queryResultSet.getCalendarValue(1, Date.class, null)).thenReturn(new Date(0L));
        TransparentMergedResult actual = new TransparentMergedResult(queryResultSet);
        assertThat(actual.getCalendarValue(1, Date.class, null), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStream() throws SQLException {
        QueryResultSet queryResultSet = mock(QueryResultSet.class);
        InputStream value = mock(InputStream.class);
        when(queryResultSet.getInputStream(1, "Ascii")).thenReturn(value);
        TransparentMergedResult actual = new TransparentMergedResult(queryResultSet);
        assertThat(actual.getInputStream(1, "Ascii"), is(value));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        TransparentMergedResult actual = new TransparentMergedResult(mock(QueryResultSet.class));
        assertFalse(actual.wasNull());
    }
}
