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

package org.apache.shardingsphere.infra.merge.result.impl.stream;

import org.apache.shardingsphere.infra.executor.sql.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.fixture.TestStreamMergedResult;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StreamMergedResultTest {
    
    private final TestStreamMergedResult streamMergedResult = new TestStreamMergedResult();
    
    @Test(expected = SQLException.class)
    public void assertGetCurrentQueryResultIfNull() throws SQLException {
        streamMergedResult.getCurrentQueryResult();
    }
    
    @Test
    public void assertGetValue() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getValue(1, Object.class)).thenReturn("1");
        streamMergedResult.setCurrentQueryResult(queryResult);
        assertThat(streamMergedResult.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetCalendarValue() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        Calendar calendar = Calendar.getInstance();
        when(queryResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        streamMergedResult.setCurrentQueryResult(queryResult);
        assertThat(streamMergedResult.getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStream() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        InputStream value = mock(InputStream.class);
        when(queryResult.getInputStream(1, "Ascii")).thenReturn(value);
        streamMergedResult.setCurrentQueryResult(queryResult);
        assertThat(streamMergedResult.getInputStream(1, "Ascii"), is(value));
    }
    
    @Test
    public void assertWasNull() {
        QueryResult queryResult = mock(QueryResult.class);
        streamMergedResult.setCurrentQueryResult(queryResult);
        assertFalse(streamMergedResult.wasNull());
    }
}
