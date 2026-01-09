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

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IteratorStreamMergedResultTest {
    
    @Test
    void assertNextWhenCurrentHasRow() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true);
        assertTrue(new IteratorStreamMergedResult(Collections.singletonList(queryResult)).next());
    }
    
    @Test
    void assertNextWhenNoResults() throws SQLException {
        assertFalse(new IteratorStreamMergedResult(Collections.singletonList(mock(QueryResult.class))).next());
    }
    
    @Test
    void assertNextWithSwitchToFollowingResult() throws SQLException {
        QueryResult first = mock(QueryResult.class);
        QueryResult second = mock(QueryResult.class);
        when(second.next()).thenReturn(true);
        assertTrue(new IteratorStreamMergedResult(Arrays.asList(first, second)).next());
    }
    
    @Test
    void assertNextUntilDataFound() throws SQLException {
        QueryResult first = mock(QueryResult.class);
        QueryResult second = mock(QueryResult.class);
        QueryResult third = mock(QueryResult.class);
        when(third.next()).thenReturn(true);
        assertTrue(new IteratorStreamMergedResult(Arrays.asList(first, second, third)).next());
    }
    
    @Test
    void assertNextFalseWhenAllEmpty() throws SQLException {
        assertFalse(new IteratorStreamMergedResult(Arrays.asList(mock(QueryResult.class), mock(QueryResult.class))).next());
    }
    
    @Test
    void assertGetValueAndTrackWasNull() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getValue(1, String.class)).thenReturn("v");
        when(queryResult.wasNull()).thenReturn(true);
        IteratorStreamMergedResult mergedResult = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        Object actual = mergedResult.getValue(1, String.class);
        assertThat(actual, is("v"));
        assertTrue(mergedResult.wasNull());
    }
    
    @Test
    void assertGetCalendarValueAndTrackWasNull() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        Calendar calendar = Calendar.getInstance();
        when(queryResult.getCalendarValue(1, String.class, calendar)).thenReturn("v");
        IteratorStreamMergedResult mergedResult = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        Object actual = mergedResult.getCalendarValue(1, String.class, calendar);
        assertThat(actual, is("v"));
        assertFalse(mergedResult.wasNull());
    }
    
    @Test
    void assertGetInputStreamAndTrackWasNull() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{1});
        when(queryResult.getInputStream(1, "stream")).thenReturn(inputStream);
        IteratorStreamMergedResult mergedResult = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        ByteArrayInputStream actual = (ByteArrayInputStream) mergedResult.getInputStream(1, "stream");
        assertThat(actual, is(inputStream));
        assertFalse(mergedResult.wasNull());
    }
    
    @Test
    void assertGetCharacterStreamAndTrackWasNull() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        Reader reader = new InputStreamReader(new ByteArrayInputStream(new byte[0]));
        when(queryResult.getCharacterStream(1)).thenReturn(reader);
        IteratorStreamMergedResult mergedResult = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        assertThat(mergedResult.getCharacterStream(1), is(reader));
        assertFalse(mergedResult.wasNull());
    }
    
    @Test
    void assertThrowWhenCurrentQueryResultIsNull() throws ReflectiveOperationException {
        QueryResult queryResult = mock(QueryResult.class);
        IteratorStreamMergedResult mergedResult = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        Plugins.getMemberAccessor().set(StreamMergedResult.class.getDeclaredField("currentQueryResult"), mergedResult, null);
        SQLException actual = assertThrows(SQLException.class, () -> mergedResult.getValue(1, Object.class));
        assertThat(actual.getMessage(), is("Current ResultSet is null, ResultSet perhaps end of next"));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.wasNull()).thenReturn(true);
        IteratorStreamMergedResult mergedResult = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        assertNull(mergedResult.getCharacterStream(1));
        assertTrue(mergedResult.wasNull());
    }
}
