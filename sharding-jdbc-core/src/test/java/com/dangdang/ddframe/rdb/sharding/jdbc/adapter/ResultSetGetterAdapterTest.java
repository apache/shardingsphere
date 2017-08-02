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

import com.dangdang.ddframe.rdb.common.sql.base.AbstractShardingJDBCDatabaseAndTableTest;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.util.JDBCTestSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class ResultSetGetterAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private List<Statement> statements = new ArrayList<>();
    
    private Map<DatabaseType, ResultSet> resultSets = new HashMap<>();
    
    private final String columnName = "user_id";
    
    @Before
    public void init() throws SQLException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getShardingDataSources().entrySet()) {
            ShardingConnection shardingConnection = each.getValue().getConnection();
            shardingConnections.add(shardingConnection);
            Statement statement = shardingConnection.createStatement();
            statements.add(statement);
            ResultSet resultSet = statement.executeQuery(JDBCTestSQL.SELECT_ORDER_BY_USER_ID_SQL);
            resultSet.next();
            resultSets.put(each.getKey(), resultSet);
        }
    }
    
    @After
    public void close() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            each.close();
        }
        for (Statement each : statements) {
            each.close();
        }
        for (ShardingConnection each : shardingConnections) {
            each.close();
        }
    }
    
    @Test
    public void assertGetBooleanForColumnIndex() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                assertTrue(each.getValue().getBoolean(1));
            }
        }
    }
    
    @Test
    public void assertGetBooleanForColumnLabel() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                assertTrue(each.getValue().getBoolean(columnName));
            }
        }
    }
    
    @Test
    public void assertGetByteForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getByte(1), is((byte) 10));
        }
    }
    
    @Test
    public void assertGetByteForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getByte(columnName), is((byte) 10));
        }
    }
    
    @Test
    public void assertGetShortForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getShort(1), is((short) 10));
        }
    }
    
    @Test
    public void assertGetShortForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getShort(columnName), is((short) 10));
        }
    }
    
    @Test
    public void assertGetIntForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getInt(1), is(10));
        }
    }
    
    @Test
    public void assertGetIntForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getInt(columnName), is(10));
        }
    }
    
    @Test
    public void assertGetLongForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getLong(1), is(10L));
        }
    }
    
    @Test
    public void assertGetLongForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getLong(columnName), is(10L));
        }
    }
    
    @Test
    public void assertGetFloatForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getFloat(1), is(10F));
        }
    }
    
    @Test
    public void assertGetFloatForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getFloat(columnName), is(10F));
        }
    }
    
    @Test
    public void assertGetDoubleForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getDouble(1), is(10D));
        }
    }
    
    @Test
    public void assertGetDoubleForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getDouble(columnName), is(10D));
        }
    }
    
    @Test
    public void assertGetStringForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getString(1), is("10"));
        }
    }
    
    @Test
    public void assertGetStringForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getString(columnName), is("10"));
        }
    }
    
    @Test
    public void assertGetBigDecimalForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getBigDecimal(1), is(new BigDecimal("10")));
        }
    }
    
    @Test
    public void assertGetBigDecimalForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getBigDecimal(columnName), is(new BigDecimal("10")));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetBigDecimalColumnIndexWithScale() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getBigDecimal(1, 2), is(new BigDecimal("10")));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetBigDecimalColumnLabelWithScale() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getBigDecimal(columnName, 2), is(new BigDecimal("10")));
        }
    }
    
    @Test
    public void assertGetBytesForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertTrue(each.getBytes(1).length > 0);
        }
    }
    
    @Test
    public void assertGetBytesForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertTrue(each.getBytes(columnName).length > 0);
        }
    }
    
    @Test
    public void assertGetDateForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(1);
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(columnName);
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateColumnIndexWithCalendar() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(1, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateColumnLabelWithCalendar() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(columnName, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(1);
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(columnName);
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeColumnIndexWithCalendar() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(1, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeColumnLabelWithCalendar() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(columnName, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampForColumnIndex() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(1);
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampForColumnLabel() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(columnName);
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampColumnIndexWithCalendar() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(1, Calendar.getInstance());
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampColumnLabelWithCalendar() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(columnName, Calendar.getInstance());
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnIndex() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                byte[] b = new byte[1];
                each.getValue().getAsciiStream(1).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnLabel() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                byte[] b = new byte[1];
                each.getValue().getAsciiStream(columnName).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetUnicodeStreamForColumnIndex() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            byte[] b = new byte[1];
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getUnicodeStream(1).read(b);
                } catch (final SQLException ignore) {
                }
            } else {
                each.getValue().getUnicodeStream(1).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetUnicodeStreamForColumnLabel() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            byte[] b = new byte[1];
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getUnicodeStream(columnName).read(b);
                } catch (final SQLException ignore) {
                }
            } else {
                each.getValue().getUnicodeStream(columnName).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnIndex() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                assertTrue(each.getValue().getBinaryStream(1).read() != -1);
            }
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnLabel() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                assertTrue(each.getValue().getBinaryStream(columnName).read() != -1);
            }
        }
    }
    
    @Test
    public void assertGetCharacterStreamForColumnIndex() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                char[] c = new char[1];
                each.getValue().getCharacterStream(1).read(c);
                assertThat(c[0], is('1'));
            }
        }
    }
    
    @Test
    public void assertGetCharacterStreamForColumnLabel() throws SQLException, IOException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                char[] c = new char[1];
                each.getValue().getCharacterStream(columnName).read(c);
                assertThat(c[0], is('1'));
            }
        }
    }
    
    @Test
    public void assertGetBlobForColumnIndex() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                assertTrue(each.getValue().getBlob(1).length() > 0);
            }
        }
    }
    
    @Test
    public void assertGetBlobForColumnLabel() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                assertTrue(each.getValue().getBlob(columnName).length() > 0);
            }
        }
    }
    
    @Test
    public void assertGetClobForColumnIndex() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                assertThat(each.getValue().getClob(1).getSubString(1, 2), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetClobForColumnLabel() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                assertThat(each.getValue().getClob(columnName).getSubString(1, 2), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetURLForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getURL(1);
    
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetURLForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getURL(columnName);
                fail("Expected an SQLException to be thrown");
            } catch (final SQLException exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetSQLXMLForColumnIndex() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getSQLXML(1);
                    fail("Expected an SQLException to be thrown");
                } catch (final SQLException exception) {
                    assertFalse(exception.getMessage().isEmpty());
                }
            } else {
                assertThat(each.getValue().getSQLXML(1).getString(), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetSQLXMLForColumnLabel() throws SQLException {
        for (Map.Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getSQLXML(columnName);
                    fail("Expected an SQLException to be thrown");
                } catch (final SQLException exception) {
                    assertFalse(exception.getMessage().isEmpty());
                }
            } else {
                assertThat(each.getValue().getSQLXML(columnName).getString(), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetObjectForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getObject(1).toString(), is("10"));
        }
    }
    
    @Test
    public void assertGetObjectForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getObject(columnName).toString(), is("10"));
        }
    }
}
