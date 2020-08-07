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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.util.JDBCTestSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PreparedStatementAdapterTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    private final List<PreparedStatement> preparedStatements = new ArrayList<>();
    
    @Before
    public void init() throws SQLException {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        preparedStatements.add(connection.prepareStatement(JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL));
    }
    
    @After
    public void close() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.close();
        }
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
    }
    
    @Test
    public void assertSetNull() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setNull(1, Types.VARCHAR);
            each.setNull(2, Types.VARCHAR, "");
            assertParameter(each, 1, null);
            assertParameter(each, 2, null);
        }
    }
    
    @Test
    public void assertSetBoolean() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setBoolean(1, true);
            assertParameter(each, 1, true);
        }
    }
    
    @Test
    public void assertSetByte() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setByte(1, (byte) 0);
            assertParameter(each, 1, (byte) 0);
        }
    }
    
    @Test
    public void assertSetShort() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setShort(1, (short) 0);
            assertParameter(each, 1, (short) 0);
        }
    }
    
    @Test
    public void assertSetInt() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setInt(1, 0);
            assertParameter(each, 1, 0);
        }
    }
    
    @Test
    public void assertSetLong() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setLong(1, 0L);
            assertParameter(each, 1, 0L);
        }
    }
    
    @Test
    public void assertSetFloat() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setFloat(1, 0.0F);
            assertParameter(each, 1, 0.0F);
        }
    }
    
    @Test
    public void assertSetDouble() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setDouble(1, 0.0D);
            assertParameter(each, 1, 0.0D);
        }
    }
    
    @Test
    public void assertSetString() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setString(1, "0");
            assertParameter(each, 1, "0");
        }
    }
    
    @Test
    public void assertSetBigDecimal() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setBigDecimal(1, BigDecimal.ZERO);
            assertParameter(each, 1, BigDecimal.ZERO);
        }
    }
    
    @Test
    public void assertSetDate() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            Date now = new Date(0L);
            each.setDate(1, now);
            each.setDate(2, now, Calendar.getInstance());
            assertParameter(each, 1, now);
            assertParameter(each, 2, now);
        }
    }
    
    @Test
    public void assertSetTime() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            Time now = new Time(0L);
            each.setTime(1, now);
            each.setTime(2, now, Calendar.getInstance());
            assertParameter(each, 1, now);
            assertParameter(each, 2, now);
        }
    }
    
    @Test
    public void assertSetTimestamp() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            Timestamp now = new Timestamp(0L);
            each.setTimestamp(1, now);
            each.setTimestamp(2, now, Calendar.getInstance());
            assertParameter(each, 1, now);
            assertParameter(each, 2, now);
        }
    }
    
    @Test
    public void assertSetBytes() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setBytes(1, new byte[]{});
            assertParameter(each, 1, new byte[]{});
        }
    }
    
    @Test
    public void assertSetBlob() throws SQLException, IOException {
        for (PreparedStatement each : preparedStatements) {
            try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
                each.setBlob(1, (Blob) null);
                each.setBlob(2, inputStream);
                each.setBlob(3, inputStream, 100L);
                assertParameter(each, 1, null);
                assertParameter(each, 2, inputStream);
                assertParameter(each, 3, inputStream);
            }
        }
    }
    
    @Test
    public void assertSetClob() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            Reader reader = new SerializableStringReader();
            each.setClob(1, (Clob) null);
            each.setClob(2, reader);
            each.setClob(3, reader, 100L);
            assertParameter(each, 1, null);
            assertParameter(each, 2, reader);
            assertParameter(each, 3, reader);
        }
    }
    
    @Test
    public void assertSetAsciiStream() throws SQLException, IOException {
        for (PreparedStatement each : preparedStatements) {
            try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
                each.setAsciiStream(1, inputStream);
                each.setAsciiStream(2, inputStream, 100);
                each.setAsciiStream(3, inputStream, 100L);
                assertParameter(each, 1, inputStream);
                assertParameter(each, 2, inputStream);
                assertParameter(each, 3, inputStream);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertSetUnicodeStream() throws SQLException, IOException {
        for (PreparedStatement each : preparedStatements) {
            try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
                each.setUnicodeStream(1, inputStream, 100);
                assertParameter(each, 1, inputStream);
            }
        }
    }
    
    @Test
    public void assertSetBinaryStream() throws SQLException, IOException {
        for (PreparedStatement each : preparedStatements) {
            try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
                each.setBinaryStream(1, inputStream);
                each.setBinaryStream(2, inputStream, 100);
                each.setBinaryStream(3, inputStream, 100L);
                assertParameter(each, 1, inputStream);
                assertParameter(each, 2, inputStream);
                assertParameter(each, 3, inputStream);
            }
        }
    }
    
    @Test
    public void assertSetCharacterStream() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setCharacterStream(1, new SerializableStringReader());
            each.setCharacterStream(2, new SerializableStringReader(), 100);
            each.setCharacterStream(3, new SerializableStringReader(), 100L);
            assertParameter(each, 1, "value");
            assertParameter(each, 2, "value");
            assertParameter(each, 3, "value");
        }
    }
    
    @Test
    public void assertSetURL() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setURL(1, null);
            assertParameter(each, 1, null);
        }
    }
    
    @Test
    public void assertSetSQLXML() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            each.setSQLXML(1, null);
            assertParameter(each, 1, null);
        }
    }
    
    @Test
    public void assertSetObject() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            Object obj = "value";
            each.setObject(1, obj);
            each.setObject(2, obj, 0);
            each.setObject(3, null);
            each.setObject(4, null);
            each.setObject(5, obj, 0, 0);
            assertParameter(each, 1, obj);
            assertParameter(each, 2, obj);
            assertParameter(each, 3, null);
            assertParameter(each, 4, null);
            assertParameter(each, 5, obj);
        }
    }
    
    @Test
    public void assertClearParameters() throws SQLException {
        for (PreparedStatement each : preparedStatements) {
            Object obj = new Object();
            each.setObject(1, obj);
            each.setObject(2, obj, 0);
            each.setObject(3, null);
            each.setObject(4, null);
            each.setObject(5, obj, 0, 0);
            assertThat(((ShardingSpherePreparedStatement) each).getParameters().size(), is(5));
            each.clearParameters();
            assertTrue(((ShardingSpherePreparedStatement) each).getParameters().isEmpty());
        }
    }
    
    private void assertParameter(final PreparedStatement actual, final int index, final Object parameter) {
        assertThat(((ShardingSpherePreparedStatement) actual).getParameters().get(index - 1), is(parameter));
    }
    
    private static class SerializableStringReader extends StringReader {
        
        SerializableStringReader() {
            super("value");
        }
    }
}
