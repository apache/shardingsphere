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

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
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
import java.util.Calendar;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PreparedStatementAdapterTest {
    
    private ShardingSpherePreparedStatement shardingSpherePreparedStatement;
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private final TrafficRule trafficRule = new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build());
    
    @Before
    public void setUp() throws SQLException {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getSchema()).thenReturn(DefaultSchema.LOGIC_NAME);
        when(connection.getContextManager().getMetaDataContexts().getMetaData(connection.getSchema()).getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(connection.getContextManager().getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class)).thenReturn(Optional.of(sqlParserRule));
        when(connection.getContextManager().getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TrafficRule.class)).thenReturn(Optional.of(trafficRule));
        shardingSpherePreparedStatement = new ShardingSpherePreparedStatement(connection, "SELECT 1");
    }
    
    @Test
    public void assertSetNull() {
        shardingSpherePreparedStatement.setNull(1, Types.VARCHAR);
        shardingSpherePreparedStatement.setNull(2, Types.VARCHAR, "");
        assertParameter(shardingSpherePreparedStatement, 1, null);
        assertParameter(shardingSpherePreparedStatement, 2, null);
    }
    
    @Test
    public void assertSetBoolean() {
        shardingSpherePreparedStatement.setBoolean(1, true);
        assertParameter(shardingSpherePreparedStatement, 1, true);
    }
    
    @Test
    public void assertSetByte() {
        shardingSpherePreparedStatement.setByte(1, (byte) 0);
        assertParameter(shardingSpherePreparedStatement, 1, (byte) 0);
    }
    
    @Test
    public void assertSetShort() {
        shardingSpherePreparedStatement.setShort(1, (short) 0);
        assertParameter(shardingSpherePreparedStatement, 1, (short) 0);
    }
    
    @Test
    public void assertSetInt() {
        shardingSpherePreparedStatement.setInt(1, 0);
        assertParameter(shardingSpherePreparedStatement, 1, 0);
    }
    
    @Test
    public void assertSetLong() {
        shardingSpherePreparedStatement.setLong(1, 0L);
        assertParameter(shardingSpherePreparedStatement, 1, 0L);
    }
    
    @Test
    public void assertSetFloat() {
        shardingSpherePreparedStatement.setFloat(1, 0.0F);
        assertParameter(shardingSpherePreparedStatement, 1, 0.0F);
    }
    
    @Test
    public void assertSetDouble() {
        shardingSpherePreparedStatement.setDouble(1, 0.0D);
        assertParameter(shardingSpherePreparedStatement, 1, 0.0D);
    }
    
    @Test
    public void assertSetString() {
        shardingSpherePreparedStatement.setString(1, "0");
        assertParameter(shardingSpherePreparedStatement, 1, "0");
    }
    
    @Test
    public void assertSetBigDecimal() {
        shardingSpherePreparedStatement.setBigDecimal(1, BigDecimal.ZERO);
        assertParameter(shardingSpherePreparedStatement, 1, BigDecimal.ZERO);
    }
    
    @Test
    public void assertSetDate() {
        Date date = new Date(0L);
        shardingSpherePreparedStatement.setDate(1, date);
        shardingSpherePreparedStatement.setDate(2, date, Calendar.getInstance());
        assertParameter(shardingSpherePreparedStatement, 1, date);
        assertParameter(shardingSpherePreparedStatement, 2, date);
    }
    
    @Test
    public void assertSetTime() {
        Time time = new Time(0L);
        shardingSpherePreparedStatement.setTime(1, time);
        shardingSpherePreparedStatement.setTime(2, time, Calendar.getInstance());
        assertParameter(shardingSpherePreparedStatement, 1, time);
        assertParameter(shardingSpherePreparedStatement, 2, time);
    }
    
    @Test
    public void assertSetTimestamp() {
        Timestamp timestamp = new Timestamp(0L);
        shardingSpherePreparedStatement.setTimestamp(1, timestamp);
        shardingSpherePreparedStatement.setTimestamp(2, timestamp, Calendar.getInstance());
        assertParameter(shardingSpherePreparedStatement, 1, timestamp);
        assertParameter(shardingSpherePreparedStatement, 2, timestamp);
    }
    
    @Test
    public void assertSetBytes() {
        shardingSpherePreparedStatement.setBytes(1, new byte[]{});
        assertParameter(shardingSpherePreparedStatement, 1, new byte[]{});
    }
    
    @Test
    public void assertSetBlob() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
            shardingSpherePreparedStatement.setBlob(1, (Blob) null);
            shardingSpherePreparedStatement.setBlob(2, inputStream);
            shardingSpherePreparedStatement.setBlob(3, inputStream, 100L);
            assertParameter(shardingSpherePreparedStatement, 1, null);
            assertParameter(shardingSpherePreparedStatement, 2, inputStream);
            assertParameter(shardingSpherePreparedStatement, 3, inputStream);
        }
    }
    
    @Test
    public void assertSetClob() {
        Reader reader = new StringReader("value");
        shardingSpherePreparedStatement.setClob(1, (Clob) null);
        shardingSpherePreparedStatement.setClob(2, reader);
        shardingSpherePreparedStatement.setClob(3, reader, 100L);
        assertParameter(shardingSpherePreparedStatement, 1, null);
        assertParameter(shardingSpherePreparedStatement, 2, reader);
        assertParameter(shardingSpherePreparedStatement, 3, reader);
    }
    
    @Test
    public void assertSetAsciiStream() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
            shardingSpherePreparedStatement.setAsciiStream(1, inputStream);
            shardingSpherePreparedStatement.setAsciiStream(2, inputStream, 100);
            shardingSpherePreparedStatement.setAsciiStream(3, inputStream, 100L);
            assertParameter(shardingSpherePreparedStatement, 1, inputStream);
            assertParameter(shardingSpherePreparedStatement, 2, inputStream);
            assertParameter(shardingSpherePreparedStatement, 3, inputStream);
        }
    }
    
    @Test
    public void assertSetUnicodeStream() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
            shardingSpherePreparedStatement.setUnicodeStream(1, inputStream, 100);
            assertParameter(shardingSpherePreparedStatement, 1, inputStream);
        }
    }
    
    @Test
    public void assertSetBinaryStream() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[]{})) {
            shardingSpherePreparedStatement.setBinaryStream(1, inputStream);
            shardingSpherePreparedStatement.setBinaryStream(2, inputStream, 100);
            shardingSpherePreparedStatement.setBinaryStream(3, inputStream, 100L);
            assertParameter(shardingSpherePreparedStatement, 1, inputStream);
            assertParameter(shardingSpherePreparedStatement, 2, inputStream);
            assertParameter(shardingSpherePreparedStatement, 3, inputStream);
        }
    }
    
    @Test
    public void assertSetCharacterStream() {
        shardingSpherePreparedStatement.setCharacterStream(1, new StringReader("value"));
        shardingSpherePreparedStatement.setCharacterStream(2, new StringReader("value"), 100);
        shardingSpherePreparedStatement.setCharacterStream(3, new StringReader("value"), 100L);
        assertParameter(shardingSpherePreparedStatement, 1, "value");
        assertParameter(shardingSpherePreparedStatement, 2, "value");
        assertParameter(shardingSpherePreparedStatement, 3, "value");
    }
    
    @Test
    public void assertSetURL() {
        shardingSpherePreparedStatement.setURL(1, null);
        assertParameter(shardingSpherePreparedStatement, 1, null);
    }
    
    @Test
    public void assertSetSQLXML() {
        shardingSpherePreparedStatement.setSQLXML(1, null);
        assertParameter(shardingSpherePreparedStatement, 1, null);
    }
    
    @Test
    public void assertSetObject() {
        Object obj = "value";
        shardingSpherePreparedStatement.setObject(1, obj);
        shardingSpherePreparedStatement.setObject(2, obj, 0);
        shardingSpherePreparedStatement.setObject(3, null);
        shardingSpherePreparedStatement.setObject(4, null);
        shardingSpherePreparedStatement.setObject(5, obj, 0, 0);
        assertParameter(shardingSpherePreparedStatement, 1, obj);
        assertParameter(shardingSpherePreparedStatement, 2, obj);
        assertParameter(shardingSpherePreparedStatement, 3, null);
        assertParameter(shardingSpherePreparedStatement, 4, null);
        assertParameter(shardingSpherePreparedStatement, 5, obj);
    }
    
    private void assertParameter(final PreparedStatement actual, final int index, final Object parameter) {
        assertThat(((ShardingSpherePreparedStatement) actual).getParameters().get(index - 1), is(parameter));
    }
    
    @Test
    public void assertClearParameters() {
        Object obj = new Object();
        shardingSpherePreparedStatement.setObject(1, obj);
        shardingSpherePreparedStatement.setObject(2, obj, 0);
        shardingSpherePreparedStatement.setObject(3, null);
        shardingSpherePreparedStatement.setObject(4, null);
        shardingSpherePreparedStatement.setObject(5, obj, 0, 0);
        assertThat(shardingSpherePreparedStatement.getParameters().size(), is(5));
        shardingSpherePreparedStatement.clearParameters();
        assertTrue(shardingSpherePreparedStatement.getParameters().isEmpty());
    }
}
