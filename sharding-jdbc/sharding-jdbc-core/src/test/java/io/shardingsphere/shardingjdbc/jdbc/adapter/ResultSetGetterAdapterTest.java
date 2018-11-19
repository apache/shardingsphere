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

package io.shardingsphere.shardingjdbc.jdbc.adapter;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.jdbc.util.JDBCTestSQL;
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
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class ResultSetGetterAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private final List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private final List<Statement> statements = new ArrayList<>();
    
    private final Map<DatabaseType, ResultSet> resultSets = new HashMap<>();
    
    private final String columnName = "user_id";
    
    @Before
    public void init() throws SQLException {
        ShardingConnection shardingConnection = getShardingDataSource().getConnection();
        shardingConnections.add(shardingConnection);
        Statement statement = shardingConnection.createStatement();
        statements.add(statement);
        ResultSet resultSet = statement.executeQuery(JDBCTestSQL.SELECT_ORDER_BY_USER_ID_SQL);
        resultSet.next();
        resultSets.put(DatabaseType.H2, resultSet);
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
    public void assertGetBooleanForColumnIndex() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getBoolean(1);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetBooleanForColumnLabel() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                assertTrue(each.getValue().getBoolean(columnName));
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
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
            try {
                assertTrue(each.getBytes(1).length > 0);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetBytesForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            try {
                assertTrue(each.getBytes(columnName).length > 0);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateForColumnIndex() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(1);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateForColumnLabel() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(columnName);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateColumnIndexWithCalendar() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(1, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetDateColumnLabelWithCalendar() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getDate(columnName, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeForColumnIndex() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(1);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeForColumnLabel() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(columnName);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeColumnIndexWithCalendar() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(1, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimeColumnLabelWithCalendar() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getTime(columnName, Calendar.getInstance());
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampForColumnIndex() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(1);
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampForColumnLabel() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(columnName);
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampColumnIndexWithCalendar() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(1, Calendar.getInstance());
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetTimestampColumnLabelWithCalendar() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                each.getValue().getTimestamp(columnName, Calendar.getInstance());
                if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                    continue;
                }
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnIndex() throws SQLException, IOException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                byte[] b = new byte[1];
                each.getValue().getAsciiStream(1).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @Test
    public void assertGetAsciiStreamForColumnLabel() throws SQLException, IOException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
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
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            byte[] b = new byte[1];
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getUnicodeStream(1).read(b);
                } catch (final Exception ignored) {
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
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            byte[] b = new byte[1];
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getUnicodeStream(columnName).read(b);
                } catch (final Exception ignored) {
                }
            } else {
                each.getValue().getUnicodeStream(columnName).read(b);
                assertThat(new String(b), is("1"));
            }
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnIndex() throws SQLException, IOException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                assertTrue(each.getValue().getBinaryStream(1).read() != -1);
            }
        }
    }
    
    @Test
    public void assertGetBinaryStreamForColumnLabel() throws SQLException, IOException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                assertTrue(each.getValue().getBinaryStream(columnName).read() != -1);
            }
        }
    }
    
    @Test
    public void assertGetCharacterStreamForColumnIndex() throws SQLException, IOException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                char[] c = new char[1];
                each.getValue().getCharacterStream(1).read(c);
                assertThat(c[0], is('1'));
            }
        }
    }
    
    @Test
    public void assertGetCharacterStreamForColumnLabel() throws SQLException, IOException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.MySQL == each.getKey() || DatabaseType.PostgreSQL == each.getKey()) {
                char[] c = new char[1];
                each.getValue().getCharacterStream(columnName).read(c);
                assertThat(c[0], is('1'));
            }
        }
    }
    
    @Test
    public void assertGetBlobForColumnIndex() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                try {
                    assertTrue(each.getValue().getBlob(1).length() > 0);
                    fail("Expected an SQLException to be thrown");
                } catch (final Exception exception) {
                    assertFalse(exception.getMessage().isEmpty());
                }
            }
        }
    }
    
    @Test
    public void assertGetBlobForColumnLabel() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.H2 == each.getKey()) {
                try {
                    assertTrue(each.getValue().getBlob(columnName).length() > 0);
                    fail("Expected an SQLException to be thrown");
                } catch (final Exception exception) {
                    assertFalse(exception.getMessage().isEmpty());
                }
            }
        }
    }
    
    @Test
    public void assertGetClobForColumnIndex() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                assertThat(each.getValue().getClob(1).getSubString(1, 2), is("10"));
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetClobForColumnLabel() {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            try {
                assertThat(each.getValue().getClob(columnName).getSubString(1, 2), is("10"));
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetURLForColumnIndex() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getURL(1);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetURLForColumnLabel() {
        for (ResultSet each : resultSets.values()) {
            try {
                each.getURL(columnName);
                fail("Expected an SQLException to be thrown");
            } catch (final Exception exception) {
                assertFalse(exception.getMessage().isEmpty());
            }
        }
    }
    
    @Test
    public void assertGetSQLXMLForColumnIndex() throws SQLException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getSQLXML(1);
                    fail("Expected an SQLException to be thrown");
                } catch (final Exception exception) {
                    assertFalse(exception.getMessage().isEmpty());
                }
            } else {
                assertThat(each.getValue().getSQLXML(1).getString(), is("10"));
            }
        }
    }
    
    @Test
    public void assertGetSQLXMLForColumnLabel() throws SQLException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseType.Oracle == each.getKey()) {
                continue;
            }
            if (DatabaseType.H2 == each.getKey() || DatabaseType.SQLServer == each.getKey()) {
                try {
                    each.getValue().getSQLXML(columnName);
                    fail("Expected an SQLException to be thrown");
                } catch (final Exception exception) {
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
