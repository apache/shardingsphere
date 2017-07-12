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

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.MySQL;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.Oracle;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.PostgreSQL;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.SQLServer;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResultSetGetterAdapterTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    private ShardingConnection shardingConnection;
    
    private Statement statement;
    
    private ResultSet actual;
    
    private String columnName = "uid";
    
    @Before
    public void init() throws SQLException {
        shardingConnection = getShardingDataSource().getConnection();
        statement = shardingConnection.createStatement();
        actual = statement.executeQuery(getDatabaseTestSQL().getSelectUserIdByStatusOrderByUserIdSql());
        actual.next();
        if (currentDbType() == Oracle) {
            columnName = "usrid";
        }
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        statement.close();
        shardingConnection.close();
    }
    
    @Test
    public void assertGetBooleanForColumnIndex() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            assertTrue(actual.getBoolean(1));
        }
    }
    
    @Test
    public void assertGetBooleanForColumnLabel() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            assertTrue(actual.getBoolean("uid"));
        }
    }
    
    @Test
    public void assertGetByteForColumnIndex() throws SQLException {
        assertThat(actual.getByte(1), is((byte) 10));
    }
    
    @Test
    public void assertGetByteForColumnLabel() throws SQLException {
        assertThat(actual.getByte(columnName), is((byte) 10));
    }
    
    @Test
    public void assertGetShortForColumnIndex() throws SQLException {
        assertThat(actual.getShort(1), is((short) 10));
    }
    
    @Test
    public void assertGetShortForColumnLabel() throws SQLException {
        assertThat(actual.getShort(columnName), is((short) 10));
    }
    
    @Test
    public void assertGetIntForColumnIndex() throws SQLException {
        assertThat(actual.getInt(1), is(10));
    }
    
    @Test
    public void assertGetIntForColumnLabel() throws SQLException {
        assertThat(actual.getInt(columnName), is(10));
    }
    
    @Test
    public void assertGetLongForColumnIndex() throws SQLException {
        assertThat(actual.getLong(1), is(10L));
    }
    
    @Test
    public void assertGetLongForColumnLabel() throws SQLException {
        assertThat(actual.getLong(columnName), is(10L));
    }
    
    @Test
    public void assertGetFloatForColumnIndex() throws SQLException {
        assertThat(actual.getFloat(1), is(10F));
    }
    
    @Test
    public void assertGetFloatForColumnLabel() throws SQLException {
        assertThat(actual.getFloat(columnName), is(10F));
    }
    
    @Test
    public void assertGetDoubleForColumnIndex() throws SQLException {
        assertThat(actual.getDouble(1), is(10D));
    }
    
    @Test
    public void assertGetDoubleForColumnLabel() throws SQLException {
        assertThat(actual.getDouble(columnName), is(10D));
    }
    
    @Test
    public void assertGetStringForColumnIndex() throws SQLException {
        assertThat(actual.getString(1), is("10"));
    }
    
    @Test
    public void assertGetStringForColumnLabel() throws SQLException {
        assertThat(actual.getString(columnName), is("10"));
    }
    
    @Test
    public void assertGetBigDecimalForColumnIndex() throws SQLException {
        assertThat(actual.getBigDecimal(1), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBigDecimalForColumnLabel() throws SQLException {
        assertThat(actual.getBigDecimal(columnName), is(new BigDecimal("10")));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetBigDecimalColumnIndexWithScale() throws SQLException {
        assertThat(actual.getBigDecimal(1, 2), is(new BigDecimal("10")));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetBigDecimalColumnLabelWithScale() throws SQLException {
        assertThat(actual.getBigDecimal(columnName, 2), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBytesForColumnIndex() throws SQLException {
        assertTrue(actual.getBytes(1).length > 0);
    }
    
    @Test
    public void assertGetBytesForColumnLabel() throws SQLException {
        assertTrue(actual.getBytes(columnName).length > 0);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateForColumnIndex() throws SQLException {
        actual.getDate(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateForColumnLabel() throws SQLException {
        actual.getDate(columnName);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateColumnIndexWithCalendar() throws SQLException {
        actual.getDate(1, Calendar.getInstance());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetDateColumnLabelWithCalendar() throws SQLException {
        actual.getDate(columnName, Calendar.getInstance());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeForColumnIndex() throws SQLException {
        actual.getTime(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeForColumnLabel() throws SQLException {
        actual.getTime(columnName);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeColumnIndexWithCalendar() throws SQLException {
        actual.getTime(1, Calendar.getInstance());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetTimeColumnLabelWithCalendar() throws SQLException {
        actual.getTime(columnName, Calendar.getInstance());
    }
    
    @Test
    public void assertGetTimestampForColumnIndex() throws SQLException {
        try {
            actual.getTimestamp(1);
        } catch (final SQLException ex) {
            if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        }
    }
    
    @Test
    public void assertGetTimestampForColumnLabel() throws SQLException {
        try {
            actual.getTimestamp(columnName);
        } catch (final SQLException ex) {
            if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        }
    }
    
    @Test
    public void assertGetTimestampColumnIndexWithCalendar() throws SQLException {
        try {
            actual.getTimestamp(1, Calendar.getInstance());
        } catch (final SQLException ex) {
            if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        }
    }
    
    @Test
    public void assertGetTimestampColumnLabelWithCalendar() throws SQLException {
        try {
            actual.getTimestamp(columnName, Calendar.getInstance());
        } catch (final SQLException ex) {
            if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
                assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            }
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnIndex() throws SQLException, IOException {
        if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
            byte[] b = new byte[1];
            actual.getAsciiStream(1).read(b);
            assertThat(new String(b), is("1"));
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnLabel() throws SQLException, IOException {
        if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
            byte[] b = new byte[1];
            actual.getAsciiStream(columnName).read(b);
            assertThat(new String(b), is("1"));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetUnicodeStreamForColumnIndex() throws SQLException, IOException {
        if (currentDbType() != Oracle) {
            byte[] b = new byte[1];
            if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE || currentDbType() == SQLServer) {
                try {
                    actual.getUnicodeStream(1).read(b);
                } catch (final SQLException ignore) {
                }
            } else {
                actual.getUnicodeStream(1).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetUnicodeStreamForColumnLabel() throws SQLException, IOException {
        if (currentDbType() != Oracle) {
            byte[] b = new byte[1];
            if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE || currentDbType() == SQLServer) {
                try {
                    actual.getUnicodeStream(columnName).read(b);
                } catch (final SQLException ignore) {
                }
            } else {
                actual.getUnicodeStream(columnName).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnIndex() throws SQLException, IOException {
        if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
            assertTrue(actual.getBinaryStream(1).read() != -1);
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnLabel() throws SQLException, IOException {
        if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
            assertTrue(actual.getBinaryStream(columnName).read() != -1);
        }
    }
    
    @Test
    public void assertGetCharacterStreamForColumnIndex() throws SQLException, IOException {
        if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
            char[] c = new char[1];
            actual.getCharacterStream(1).read(c);
            assertThat(c[0], is('1'));
        }
    }
    
    @Test
    public void assertGetCharacterStreamForColumnLabel() throws SQLException, IOException {
        if (currentDbType() == MySQL || currentDbType() == PostgreSQL) {
            char[] c = new char[1];
            actual.getCharacterStream(columnName).read(c);
            assertThat(c[0], is('1'));
        }
    }
    
    @Test
    public void assertGetBlobForColumnIndex() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            assertTrue(actual.getBlob(1).length() > 0);
        }
    }
    
    @Test
    public void assertGetBlobForColumnLabel() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            assertTrue(actual.getBlob(columnName).length() > 0);
        }
    }
    
    @Test
    public void assertGetClobForColumnIndex() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            assertThat(actual.getClob(1).getSubString(1, 2), is("10"));
        }
    }
    
    @Test
    public void assertGetClobForColumnLabel() throws SQLException {
        if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
            assertThat(actual.getClob(columnName).getSubString(1, 2), is("10"));
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertGetURLForColumnIndex() throws SQLException {
        actual.getURL(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetURLForColumnLabel() throws SQLException {
        actual.getURL(columnName);
    }
    
    @Test
    public void assertGetSQLXMLForColumnIndex() throws SQLException {
        if (currentDbType() != Oracle) {
            if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE || currentDbType() == SQLServer) {
                try {
                    actual.getSQLXML(1);
                } catch (final SQLException ignore) {
                }
            } else {
                assertThat(actual.getSQLXML(1).getString(), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetSQLXMLForColumnLabel() throws SQLException {
        if (currentDbType() != Oracle) {
            if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE || currentDbType() == SQLServer) {
                try {
                    actual.getSQLXML(columnName);
                } catch (final SQLException ignore) {
                }
            } else {
                assertThat(actual.getSQLXML(columnName).getString(), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetObjectForColumnIndex() throws SQLException {
        assertThat(actual.getObject(1).toString(), is("10"));
    }
    
    @Test
    public void assertGetObjectForColumnLabel() throws SQLException {
        assertThat(actual.getObject(columnName).toString(), is("10"));
    }
}
