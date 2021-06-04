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

package org.apache.shardingsphere.sharding.merge.dal.common;

import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MultipleLocalDataMergedResultTest {
    
    @Test
    public void assertNext() {
        List<Object> row = Collections.singletonList("value");
        MultipleLocalDataMergedResult actual = new MultipleLocalDataMergedResult(Collections.singletonList(row));
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValue() {
        List<Object> row = Collections.singletonList("value");
        MultipleLocalDataMergedResult actual = new MultipleLocalDataMergedResult(Collections.singletonList(row));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("value"));
    }
    
    @Test
    public void assertGetCalendarValue() {
        List<Object> row = Collections.singletonList(new Date(0L));
        MultipleLocalDataMergedResult actual = new MultipleLocalDataMergedResult(Collections.singletonList(row));
        assertTrue(actual.next());
        assertThat(actual.getCalendarValue(1, Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStream() throws SQLException {
        List<Object> row = Collections.singletonList("value");
        MultipleLocalDataMergedResult actual = new MultipleLocalDataMergedResult(Collections.singletonList(row));
        actual.getInputStream(1, "Ascii");
    }
    
    @Test
    public void assertWasNull() {
        List<Object> row = Collections.singletonList("value");
        MultipleLocalDataMergedResult actual = new MultipleLocalDataMergedResult(Collections.singletonList(row));
        assertTrue(actual.next());
        assertFalse(actual.wasNull());
    }
}
