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

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Date;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransparentMergedResultTest {
    
    @Test
    void assertNext() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true, false);
        TransparentMergedResult actual = new TransparentMergedResult(queryResult);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValue() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getValue(1, Object.class)).thenReturn("1");
        TransparentMergedResult actual = new TransparentMergedResult(queryResult);
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    void assertGetCalendarValue() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getCalendarValue(1, Date.class, null)).thenReturn(new Date(0L));
        TransparentMergedResult actual = new TransparentMergedResult(queryResult);
        assertThat(actual.getCalendarValue(1, Date.class, null), is(new Date(0L)));
    }
    
    @Test
    void assertGetInputStream() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        InputStream value = mock(InputStream.class);
        when(queryResult.getInputStream(1, "Ascii")).thenReturn(value);
        TransparentMergedResult actual = new TransparentMergedResult(queryResult);
        assertThat(actual.getInputStream(1, "Ascii"), is(value));
    }
    
    @Test
    void assertGetCharacterStream() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        Reader value = mock(Reader.class);
        when(queryResult.getCharacterStream(1)).thenReturn(value);
        TransparentMergedResult actual = new TransparentMergedResult(queryResult);
        assertThat(actual.getCharacterStream(1), is(value));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        TransparentMergedResult actual = new TransparentMergedResult(mock(QueryResult.class));
        assertFalse(actual.wasNull());
    }
}
