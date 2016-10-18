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

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingPreparedStatement;
import com.mysql.jdbc.Blob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PreparedStatementAdapterTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingConnection shardingConnection;
    
    private PreparedStatement actual;
    
    @Before
    public void init() throws SQLException {
        shardingConnection = getShardingDataSource().getConnection();
        actual = shardingConnection.prepareStatement("SELECT user_id AS `uid` FROM `t_order` WHERE `status` IN (? ,? ,? ,? ,?)");
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        shardingConnection.close();
    }
    
    @Test
    public void assertSetNull() throws SQLException {
        actual.setNull(1, Types.VARCHAR);
        actual.setNull(2, Types.VARCHAR, "");
        assertParameter(actual, 1, null);
        assertParameter(actual, 2, null);
    }
    
    @Test
    public void assertSetBoolean() throws SQLException {
        actual.setBoolean(1, true);
        assertParameter(actual, 1, true);
    }
    
    @Test
    public void assertSetByte() throws SQLException {
        actual.setByte(1, (byte) 0);
        assertParameter(actual, 1, (byte) 0);
    }
    
    @Test
    public void assertSetShort() throws SQLException {
        actual.setShort(1, (short) 0);
        assertParameter(actual, 1, (short) 0);
    }
    
    @Test
    public void assertSetInt() throws SQLException {
        actual.setInt(1, 0);
        assertParameter(actual, 1, 0);
    }
    
    @Test
    public void assertSetLong() throws SQLException {
        actual.setLong(1, 0L);
        assertParameter(actual, 1, 0L);
    }
    
    @Test
    public void assertSetFloat() throws SQLException {
        actual.setFloat(1, 0F);
        assertParameter(actual, 1, 0F);
    }
    
    @Test
    public void assertSetDouble() throws SQLException {
        actual.setDouble(1, 0D);
        assertParameter(actual, 1, 0D);
    }
    
    @Test
    public void assertSetString() throws SQLException {
        actual.setString(1, "0");
        assertParameter(actual, 1, "0");
    }
    
    @Test
    public void assertSetBigDecimal() throws SQLException {
        actual.setBigDecimal(1, BigDecimal.ZERO);
        assertParameter(actual, 1, BigDecimal.ZERO);
    }
    
    @Test
    public void assertSetDate() throws SQLException {
        Date now = new Date(0L);
        actual.setDate(1, now);
        actual.setDate(2, now, Calendar.getInstance());
        assertParameter(actual, 1, now);
        assertParameter(actual, 2, now);
    }
    
    @Test
    public void assertSetTime() throws SQLException {
        Time now = new Time(0L);
        actual.setTime(1, now);
        actual.setTime(2, now, Calendar.getInstance());
        assertParameter(actual, 1, now);
        assertParameter(actual, 2, now);
    }
    
    @Test
    public void assertSetTimestamp() throws SQLException {
        Timestamp now = new Timestamp(0L);
        actual.setTimestamp(1, now);
        actual.setTimestamp(2, now, Calendar.getInstance());
        assertParameter(actual, 1, now);
        assertParameter(actual, 2, now);
    }
    
    @Test
    public void assertSetBytes() throws SQLException {
        actual.setBytes(1, new byte[] {});
        assertParameter(actual, 1, new byte[] {});
    }
    
    @Test
    public void assertSetBlob() throws SQLException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            actual.setBlob(1, (Blob) null);
            actual.setBlob(2, inputStream);
            actual.setBlob(3, inputStream, 100L);
            assertParameter(actual, 1, null);
            assertParameter(actual, 2, inputStream);
            assertParameter(actual, 3, inputStream);
        }
    }
    
    @Test
    public void assertSetClob() throws SQLException {
        Reader reader = new SerializableStringReader();
        actual.setClob(1, (Clob) null);
        actual.setClob(2, reader);
        actual.setClob(3, reader, 100L);
        assertParameter(actual, 1, null);
        assertParameter(actual, 2, reader);
        assertParameter(actual, 3, reader);
    }
    
    @Test
    public void assertSetAsciiStream() throws SQLException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            actual.setAsciiStream(1, inputStream);
            actual.setAsciiStream(2, inputStream, 100);
            actual.setAsciiStream(3, inputStream, 100L);
            assertParameter(actual, 1, inputStream);
            assertParameter(actual, 2, inputStream);
            assertParameter(actual, 3, inputStream);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertSetUnicodeStream() throws SQLException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            actual.setUnicodeStream(1, inputStream, 100);
            assertParameter(actual, 1, inputStream);
        }
    }
    
    @Test
    public void assertSetBinaryStream() throws SQLException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            actual.setBinaryStream(1, inputStream);
            actual.setBinaryStream(2, inputStream, 100);
            actual.setBinaryStream(3, inputStream, 100L);
            assertParameter(actual, 1, inputStream);
            assertParameter(actual, 2, inputStream);
            assertParameter(actual, 3, inputStream);
        }
    }
    
    @Test
    public void assertSetCharacterStream() throws SQLException {
        Reader reader = new SerializableStringReader();
        actual.setCharacterStream(1, reader);
        actual.setCharacterStream(2, reader, 100);
        actual.setCharacterStream(3, reader, 100L);
        assertParameter(actual, 1, reader);
        assertParameter(actual, 2, reader);
        assertParameter(actual, 3, reader);
    }
    
    @Test
    public void assertSetURL() throws SQLException {
        actual.setURL(1, null);
        assertParameter(actual, 1, null);
    }
    
    @Test
    public void assertSetSQLXML() throws SQLException {
        actual.setSQLXML(1, null);
        assertParameter(actual, 1, null);
    }
    
    @Test
    public void assertSetRef() throws SQLException {
        actual.setRef(1, null);
        assertParameter(actual, 1, null);
    }
    
    @Test
    public void assertSetObject() throws SQLException {
        Object obj = "value";
        actual.setObject(1, obj);
        actual.setObject(2, obj, 0);
        actual.setObject(5, obj, 0, 0);
        assertParameter(actual, 1, obj);
        assertParameter(actual, 2, obj);
        assertParameter(actual, 3, null);
        assertParameter(actual, 4, null);
        assertParameter(actual, 5, obj);
    }
    
    @Test
    public void assertClearParameters() throws SQLException {
        Object obj = new Object();
        actual.setObject(1, obj);
        actual.setObject(2, obj, 0);
        actual.setObject(5, obj, 0, 0);
        assertThat(((ShardingPreparedStatement) actual).getParameters().size(), is(5));
        actual.clearParameters();
        assertTrue(((ShardingPreparedStatement) actual).getParameters().isEmpty());
    }
    
    private void assertParameter(final PreparedStatement actual, final int index, final Object parameter) {
        assertThat(((ShardingPreparedStatement) actual).getParameters().get(index - 1), is(parameter));
    }
    
    private static class SerializableStringReader extends StringReader implements Serializable {
        
        private static final long serialVersionUID = 5054305161835171548L;
        
        SerializableStringReader() {
            super("value");
        }
    }
}
