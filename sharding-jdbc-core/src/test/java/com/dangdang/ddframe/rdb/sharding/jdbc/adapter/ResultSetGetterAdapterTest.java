/**
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

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;

public final class ResultSetGetterAdapterTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    private ShardingConnection shardingConnection;
    
    private Statement statement;
    
    private ResultSet actual;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
        shardingConnection = shardingDataSource.getConnection();
        statement = shardingConnection.createStatement();
        actual = statement.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init' ORDER BY `uid`");
        actual.next();
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        statement.close();
        shardingConnection.close();
    }
    
    @Test
    public void assertGetBooleanForColumnIndex() throws SQLException {
        assertTrue(actual.getBoolean(1));
    }
    
    @Test
    public void assertGetBooleanForColumnLabel() throws SQLException {
        assertTrue(actual.getBoolean("uid"));
    }
    
    @Test
    public void assertGetByteForColumnIndex() throws SQLException {
        assertThat(actual.getByte(1), is((byte) 10));
    }
    
    @Test
    public void assertGetByteForColumnLabel() throws SQLException {
        assertThat(actual.getByte("uid"), is((byte) 10));
    }
    
    @Test
    public void assertGetShortForColumnIndex() throws SQLException {
        assertThat(actual.getShort(1), is((short) 10));
    }
    
    @Test
    public void assertGetShortForColumnLabel() throws SQLException {
        assertThat(actual.getShort("uid"), is((short) 10));
    }
    
    @Test
    public void assertGetIntForColumnIndex() throws SQLException {
        assertThat(actual.getInt(1), is(10));
    }
    
    @Test
    public void assertGetIntForColumnLabel() throws SQLException {
        assertThat(actual.getInt("uid"), is(10));
    }
    
    @Test
    public void assertGetLongForColumnIndex() throws SQLException {
        assertThat(actual.getLong(1), is(10L));
    }
    
    @Test
    public void assertGetLongForColumnLabel() throws SQLException {
        assertThat(actual.getLong("uid"), is(10L));
    }
    
    @Test
    public void assertGetFloatForColumnIndex() throws SQLException {
        assertThat(actual.getFloat(1), is(10F));
    }
    
    @Test
    public void assertGetFloatForColumnLabel() throws SQLException {
        assertThat(actual.getFloat("uid"), is(10F));
    }
    
    @Test
    public void assertGetDoubleForColumnIndex() throws SQLException {
        assertThat(actual.getDouble(1), is(10D));
    }
    
    @Test
    public void assertGetDoubleForColumnLabel() throws SQLException {
        assertThat(actual.getDouble("uid"), is(10D));
    }
    
    @Test
    public void assertGetStringForColumnIndex() throws SQLException {
        assertThat(actual.getString(1), is("10"));
    }
    
    @Test
    public void assertGetStringForColumnLabel() throws SQLException {
        assertThat(actual.getString("uid"), is("10"));
    }
    
    @Test
    public void assertGetBigDecimalForColumnIndex() throws SQLException {
        assertThat(actual.getBigDecimal(1), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBigDecimalForColumnLabel() throws SQLException {
        assertThat(actual.getBigDecimal("uid"), is(new BigDecimal("10")));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetBigDecimalColumnIndexWithScale() throws SQLException {
        assertThat(actual.getBigDecimal(1, 2), is(new BigDecimal("10")));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetBigDecimalColumnLabelWithScale() throws SQLException {
        assertThat(actual.getBigDecimal("uid", 2), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBytesForColumnIndex() throws SQLException {
        assertTrue(actual.getBytes(1).length > 0);
    }
    
    @Test
    public void assertGetBytesForColumnLabel() throws SQLException {
        assertTrue(actual.getBytes("uid").length > 0);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateForColumnIndex() throws SQLException {
        actual.getDate(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateForColumnLabel() throws SQLException {
        actual.getDate("uid");
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateColumnIndexWithCalendar() throws SQLException {
        actual.getDate(1, Calendar.getInstance());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateColumnLabelWithCalendar() throws SQLException {
        actual.getDate("uid", Calendar.getInstance());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeForColumnIndex() throws SQLException {
        actual.getTime(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeForColumnLabel() throws SQLException {
        actual.getTime("uid");
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeColumnIndexWithCalendar() throws SQLException {
        actual.getTime(1, Calendar.getInstance());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeColumnLabelWithCalendar() throws SQLException {
        actual.getTime("uid", Calendar.getInstance());
    }
    
    @Test
    public void assertGetTimestampForColumnIndex() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getTimestamp(1);
            } catch (final SQLException ex) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        } else {
            assertTrue(actual.getTimestamp(1).getTime() > 0);
        }
    }
    
    @Test
    public void assertGetTimestampForColumnLabel() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getTimestamp("uid");
            } catch (final SQLException ex) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        } else {
            assertTrue(actual.getTimestamp("uid").getTime() > 0);
        }
    }
    
    @Test
    public void assertGetTimestampColumnIndexWithCalendar() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getTimestamp(1, Calendar.getInstance());
            } catch (final SQLException ex) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        } else {
            assertTrue(actual.getTimestamp(1, Calendar.getInstance()).getTime() > 0);
        }
    }
    
    @Test
    public void assertGetTimestampColumnLabelWithCalendar() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getTimestamp("uid", Calendar.getInstance());
            } catch (final SQLException ex) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        } else {
            assertTrue(actual.getTimestamp("uid", Calendar.getInstance()).getTime() > 0);
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnIndex() throws SQLException, IOException {
        byte[] b = new byte[1];
        actual.getAsciiStream(1).read(b);
        assertThat(new String(b), is("1"));
    }
    
    @Test
    public void assertGetAsciiStreamForColumnLabel() throws SQLException, IOException {
        byte[] b = new byte[1];
        actual.getAsciiStream("uid").read(b);
        assertThat(new String(b), is("1"));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetUnicodeStreamForColumnIndex() throws SQLException, IOException {
        byte[] b = new byte[1];
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getUnicodeStream(1).read(b);
            } catch (final SQLException ignore) {
            }
        } else {
            actual.getUnicodeStream(1).read(b);
            assertThat(new String(b), is("1"));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetUnicodeStreamForColumnLabel() throws SQLException, IOException {
        byte[] b = new byte[1];
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getUnicodeStream("uid").read(b);
            } catch (final SQLException ignore) {
            }
        } else {
            actual.getUnicodeStream("uid").read(b);
            assertThat(new String(b), is("1"));
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnIndex() throws SQLException, IOException {
        assertTrue(actual.getBinaryStream(1).read() != -1);
    }
    
    @Test
    public void assertGetBinaryStreamForColumnLabel() throws SQLException, IOException {
        assertTrue(actual.getBinaryStream("uid").read() != -1);
    }
    
    @Test
    public void assertGetCharacterStreamForColumnIndex() throws SQLException, IOException {
        char[] c = new char[1];
        actual.getCharacterStream(1).read(c);
        assertThat(c[0], is('1'));
    }
    
    @Test
    public void assertGetCharacterStreamForColumnLabel() throws SQLException, IOException {
        char[] c = new char[1];
        actual.getCharacterStream("uid").read(c);
        assertThat(c[0], is('1'));
    }
    
    @Test
    public void assertGetBlobForColumnIndex() throws SQLException {
        assertTrue(actual.getBlob(1).length() > 0);
    }
    
    @Test
    public void assertGetBlobForColumnLabel() throws SQLException {
        assertTrue(actual.getBlob("uid").length() > 0);
    }
    
    @Test
    public void assertGetClobForColumnIndex() throws SQLException {
        assertThat(actual.getClob(1).getSubString(1, 2), is("10"));
    }
    
    @Test
    public void assertGetClobForColumnLabel() throws SQLException {
        assertThat(actual.getClob("uid").getSubString(1, 2), is("10"));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetURLForColumnIndex() throws SQLException {
        actual.getURL(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetURLForColumnLabel() throws SQLException {
        actual.getURL("uid");
    }
    
    @Test
    public void assertGetSQLXMLForColumnIndex() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getSQLXML(1);
            } catch (final SQLException ignore) {
            }
        } else {
            assertThat(actual.getSQLXML(1).getString(), is("10"));
        }
    }
    
    @Test
    public void assertGetSQLXMLForColumnLabel() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getSQLXML("uid");
            } catch (final SQLException ignore) {
            }
        } else {
            assertThat(actual.getSQLXML("uid").getString(), is("10"));
        }
    }
    
    @Test
    public void assertGetObjectForColumnIndex() throws SQLException {
        assertThat(actual.getObject(1).toString(), is("10"));
    }
    
    @Test
    public void assertGetObjectForColumnLabel() throws SQLException {
        assertThat(actual.getObject("uid").toString(), is("10"));
    }
    
    @Test
    public void assertGetObjectForColumnIndexWithMap() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getObject("1", Collections.<String, Class<?>>emptyMap());
            } catch (final SQLException ignore) {
            }
        } else {
            assertThat(actual.getObject("uid", Collections.<String, Class<?>>emptyMap()).toString(), is("10"));
        }
    }
    
    @Test
    public void assertGetObjectForColumnLabelWithMap() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            try {
                actual.getObject("uid", Collections.<String, Class<?>>emptyMap());
            } catch (final SQLException ignore) {
            }
        } else {
            assertThat(actual.getObject("uid", Collections.<String, Class<?>>emptyMap()).toString(), is("10"));
        }
    }
}
