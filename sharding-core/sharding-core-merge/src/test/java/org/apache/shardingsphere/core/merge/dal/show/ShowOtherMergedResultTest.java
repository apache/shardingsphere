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

import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShowOtherMergedResultTest {
    
    @Test
    public void assertNext() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true, false);
        ShowOtherMergedResult actual = new ShowOtherMergedResult(queryResult);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValue() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getValue(1, Object.class)).thenReturn("1");
        ShowOtherMergedResult actual = new ShowOtherMergedResult(queryResult);
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValue() throws SQLException {
        ShowOtherMergedResult actual = new ShowOtherMergedResult(mock(QueryResult.class));
        actual.getCalendarValue(1, Date.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStream() throws SQLException {
        ShowOtherMergedResult actual = new ShowOtherMergedResult(mock(QueryResult.class));
        actual.getInputStream(1, "Ascii");
    }
    
    @Test
    public void assertWasNull() {
        ShowOtherMergedResult actual = new ShowOtherMergedResult(mock(QueryResult.class));
        assertFalse(actual.wasNull());
    }
}
