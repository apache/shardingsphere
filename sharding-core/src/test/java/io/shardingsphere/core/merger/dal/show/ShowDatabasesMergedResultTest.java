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

package io.shardingsphere.core.merger.dal.show;

import io.shardingsphere.core.constant.ShardingConstant;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public final class ShowDatabasesMergedResultTest {
    
    private ShowDatabasesMergedResult showDatabasesMergedResult;
    
    @Before
    public void setUp() {
        showDatabasesMergedResult = new ShowDatabasesMergedResult();
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
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        assertTrue(showDatabasesMergedResult.next());
        assertThat(showDatabasesMergedResult.getValue("label", Object.class).toString(), is(ShardingConstant.LOGIC_SCHEMA_NAME));
    
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValueWithColumnLabel() throws SQLException {
        showDatabasesMergedResult.getCalendarValue("label", Object.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getInputStream(1, "Ascii");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnLabel() throws SQLException {
        showDatabasesMergedResult.getInputStream("label", "Ascii");
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(showDatabasesMergedResult.wasNull());
    }
}
