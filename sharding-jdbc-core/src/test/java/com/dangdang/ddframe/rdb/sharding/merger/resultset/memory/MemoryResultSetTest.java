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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory;

import com.dangdang.ddframe.rdb.sharding.jdbc.fixture.MockRowSetResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class MemoryResultSetTest {
    
    private final Object input;
    
    private final String methodName;
    
    private final Object result;
    
    private final int scale;
    
    private final Calendar cal;
    
    @Parameterized.Parameters(name = "{index}: input:{0}, method:{1}, result:{2}")
    public static Collection init() throws MalformedURLException {
        Date now = new Date();
        return Arrays.asList(new Object[][]{
                {1, "getByte", (byte) 1, 0, null},
                {1, "getShort", (short) 1, 0, null},
                {1, "getInt", 1, 0, null},
                {1, "getLong", 1L, 0, null},
                {1, "getFloat", 1F, 0, null},
                {1, "getDouble", 1D, 0, null},
                {1.1231, "getBigDecimal", new BigDecimal("1.1231"), 0, null},
                {1.125, "getBigDecimal", new BigDecimal("1.13"), 2, null},
                {true, "getBoolean", true, 0, null},
                {"false", "getBoolean", false, 0, null},
                {null, "getBoolean", false, 0, null},
                {"1", "getString", "1", 0, null},
                {"1", "getBytes", "1".getBytes(), 0, null},
                {null, "getBytes", null, 0, null},
                {1, "getObject", 1, 0, null},
                {now, "getDate", now, 0, null},
                {now, "getTime", now, 0, null},
                {now, "getTimestamp", new Timestamp(now.getTime()), 0, null},
                {now, "getDate", now, 0, Calendar.getInstance()},
                {now, "getTime", now, 0, Calendar.getInstance()},
                {now, "getTimestamp", new Timestamp(now.getTime()), 0, Calendar.getInstance()},
                {"http://www.dangdang.com", "getURL", new URL("http://www.dangdang.com"), 0, null},
                {null, "getURL", null, 0, null},
        });
    }
    
    @Test
    public void test() throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MockResultSet mockResultSet = new MockResultSet<>(input);
        MockRowSetResultSet rs = new MockRowSetResultSet(mockResultSet);
        assertThat(rs.getStatement(), nullValue());
        assertThat(rs.getFetchSize(), is(1));
        assertThat(rs.getMetaData().getColumnCount(), is(1));
        assertThat(rs.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
        assertThat(rs.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
        assertThat(rs.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
        rs.clearWarnings();
        assertThat(rs.getWarnings(), nullValue());
        
        assertThat(rs.next(), is(true));
        if (scale > 0) {
            assertThat(ResultSet.class.getMethod(methodName, int.class, int.class).invoke(rs, 1, scale), is(result));
            assertThat(ResultSet.class.getMethod(methodName, String.class, int.class).invoke(rs, "name", scale), is(result));
        } else if (null != cal) {
            assertThat(ResultSet.class.getMethod(methodName, int.class, Calendar.class).invoke(rs, 1, cal), is(result));
            assertThat(ResultSet.class.getMethod(methodName, String.class, Calendar.class).invoke(rs, "name", cal), is(result));
        } else {
            assertThat(ResultSet.class.getMethod(methodName, int.class).invoke(rs, 1), is(result));
            assertThat(ResultSet.class.getMethod(methodName, String.class).invoke(rs, "name"), is(result));
        }
        if (null == result || null == input) {
            assertThat(rs.wasNull(), is(true));
        } else {
            assertThat(rs.wasNull(), is(false));
        }
        assertThat(rs.next(), is(false));
        assertThat(rs.isClosed(), is(false));
        rs.close();
        assertThat(rs.isClosed(), is(true));
    }
}
