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
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
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
        when(connection.getDatabaseName()).thenReturn(DefaultSchema.LOGIC_NAME);
        when(connection.getContextManager().getMetaDataContexts().getMetaData(connection.getDatabaseName()).getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
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
    
    @Test
    public void assertSetObjectForNull(){
        Object obj = "value";
        shardingSpherePreparedStatement.setObject(1,obj,Types.NULL);
        assertParameter(shardingSpherePreparedStatement, 1, obj);
    }
    @Test
    public void assertSetObjectForInteger(){
        shardingSpherePreparedStatement.setObject(1,1,Types.INTEGER);
        shardingSpherePreparedStatement.setObject(2,"1",Types.INTEGER);
        shardingSpherePreparedStatement.setObject(3,1L,Types.INTEGER);
        shardingSpherePreparedStatement.setObject(4,1D,Types.INTEGER);
        shardingSpherePreparedStatement.setObject(5,1F,Types.INTEGER);
        shardingSpherePreparedStatement.setObject(6,'1',Types.INTEGER);
        shardingSpherePreparedStatement.setObject(7,true,Types.INTEGER);
        assertParameter(shardingSpherePreparedStatement,1,1);
        assertParameter(shardingSpherePreparedStatement,2,1);
        assertParameter(shardingSpherePreparedStatement,3,1);
        assertParameter(shardingSpherePreparedStatement,4,1);
        assertParameter(shardingSpherePreparedStatement,5,1);
        assertParameter(shardingSpherePreparedStatement,6,1);
        assertParameter(shardingSpherePreparedStatement,7,1);
    }
    @Test
    public void assertSetObjectForShort(){
        shardingSpherePreparedStatement.setObject(1,1,Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(2,"1",Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(3,1L,Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(4,1D,Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(5,1F,Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(6,'1',Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(7,true,Types.SMALLINT);
        shardingSpherePreparedStatement.setObject(8,1,Types.TINYINT);
        shardingSpherePreparedStatement.setObject(9,"1",Types.TINYINT);
        shardingSpherePreparedStatement.setObject(10,1L,Types.TINYINT);
        shardingSpherePreparedStatement.setObject(11,1D,Types.TINYINT);
        shardingSpherePreparedStatement.setObject(12,1F,Types.TINYINT);
        shardingSpherePreparedStatement.setObject(13,'1',Types.TINYINT);
        shardingSpherePreparedStatement.setObject(14,true,Types.TINYINT);
        short value = 1;
        assertParameter(shardingSpherePreparedStatement,1,value);
        assertParameter(shardingSpherePreparedStatement,2,value);
        assertParameter(shardingSpherePreparedStatement,3,value);
        assertParameter(shardingSpherePreparedStatement,4,value);
        assertParameter(shardingSpherePreparedStatement,5,value);
        assertParameter(shardingSpherePreparedStatement,6,value);
        assertParameter(shardingSpherePreparedStatement,7,value);
        assertParameter(shardingSpherePreparedStatement,8,value);
        assertParameter(shardingSpherePreparedStatement,9,value);
        assertParameter(shardingSpherePreparedStatement,10,value);
        assertParameter(shardingSpherePreparedStatement,11,value);
        assertParameter(shardingSpherePreparedStatement,12,value);
        assertParameter(shardingSpherePreparedStatement,13,value);
        assertParameter(shardingSpherePreparedStatement,14,value);
    }
    @Test
    public void assertSetObjectForLong(){
        java.util.Date date = new java.util.Date();
        shardingSpherePreparedStatement.setObject(1,1,Types.BIGINT);
        shardingSpherePreparedStatement.setObject(2,"1",Types.BIGINT);
        shardingSpherePreparedStatement.setObject(3,1L,Types.BIGINT);
        shardingSpherePreparedStatement.setObject(4,1D,Types.BIGINT);
        shardingSpherePreparedStatement.setObject(5,1F,Types.BIGINT);
        shardingSpherePreparedStatement.setObject(6,'1',Types.BIGINT);
        shardingSpherePreparedStatement.setObject(7,true,Types.BIGINT);
        shardingSpherePreparedStatement.setObject(8,date,Types.BIGINT);
        assertParameter(shardingSpherePreparedStatement,1,1L);
        assertParameter(shardingSpherePreparedStatement,2,1L);
        assertParameter(shardingSpherePreparedStatement,3,1L);
        assertParameter(shardingSpherePreparedStatement,4,1L);
        assertParameter(shardingSpherePreparedStatement,5,1L);
        assertParameter(shardingSpherePreparedStatement,6,1L);
        assertParameter(shardingSpherePreparedStatement,7,1L);
        assertParameter(shardingSpherePreparedStatement,8,date.getTime());
    }

    @Test
    public void assertSetObjectForString() throws Exception{
        String value = "value";
        shardingSpherePreparedStatement.setObject(1,value,Types.VARCHAR);
        shardingSpherePreparedStatement.setObject(2,value,Types.LONGNVARCHAR);
        shardingSpherePreparedStatement.setObject(3,value,Types.CHAR);
        assertParameter(shardingSpherePreparedStatement,1,value);
        assertParameter(shardingSpherePreparedStatement,2,value);
        assertParameter(shardingSpherePreparedStatement,3,value);
    }

    @Test
    public void assertSetObjectForDouble(){
        java.util.Date date = new java.util.Date();
        shardingSpherePreparedStatement.setObject(1,"1.1",Types.FLOAT);
        shardingSpherePreparedStatement.setObject(2,1.1F,Types.FLOAT);
        shardingSpherePreparedStatement.setObject(3,1.1D,Types.FLOAT);
        shardingSpherePreparedStatement.setObject(4,1,Types.FLOAT);
        shardingSpherePreparedStatement.setObject(5,true,Types.FLOAT);
        shardingSpherePreparedStatement.setObject(6,'1',Types.FLOAT);
        shardingSpherePreparedStatement.setObject(7,date,Types.FLOAT);
        shardingSpherePreparedStatement.setObject(8,BigDecimal.valueOf(1.1d),Types.FLOAT);
        shardingSpherePreparedStatement.setObject(9,"1.1",Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(10,1.1F,Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(11,1.1D,Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(12,1,Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(13,true,Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(14,'1',Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(15,date,Types.DOUBLE);
        shardingSpherePreparedStatement.setObject(16,BigDecimal.valueOf(1.1d),Types.DOUBLE);
        assertParameter(shardingSpherePreparedStatement,1,1.1D);
        assertParameter(shardingSpherePreparedStatement,2,1.1D);
        assertParameter(shardingSpherePreparedStatement,3,1.1D);
        assertParameter(shardingSpherePreparedStatement,4,1D);
        assertParameter(shardingSpherePreparedStatement,5,1D);
        assertParameter(shardingSpherePreparedStatement,6,1D);
        assertParameter(shardingSpherePreparedStatement,7,new Long(date.getTime()).doubleValue());
        assertParameter(shardingSpherePreparedStatement,8,1.1D);
        assertParameter(shardingSpherePreparedStatement,9,1.1D);
        assertParameter(shardingSpherePreparedStatement,10,1.1D);
        assertParameter(shardingSpherePreparedStatement,11,1.1D);
        assertParameter(shardingSpherePreparedStatement,12,1D);
        assertParameter(shardingSpherePreparedStatement,13,1D);
        assertParameter(shardingSpherePreparedStatement,13,1D);
        assertParameter(shardingSpherePreparedStatement,15,new Long(date.getTime()).doubleValue());
        assertParameter(shardingSpherePreparedStatement,16,1.1D);
    }

    @Test
    public void assertSetObjectForBigDecimal(){
        java.util.Date date = new java.util.Date();
        shardingSpherePreparedStatement.setObject(1,"1.1",Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(2,1.1F,Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(3,1.1D,Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(4,1,Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(5,true,Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(6,'1',Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(7,date,Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(8,BigDecimal.valueOf(1.1d),Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(9,"1.1",Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(10,1.1F,Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(11,1.1D,Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(12,1,Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(13,true,Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(14,'1',Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(15,date,Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(16,BigDecimal.valueOf(1.1d),Types.DECIMAL);
        shardingSpherePreparedStatement.setObject(17, BigInteger.valueOf(1L),Types.NUMERIC);
        shardingSpherePreparedStatement.setObject(18, BigInteger.valueOf(1L),Types.DECIMAL);
        BigDecimal one = BigDecimal.ONE;
        BigDecimal onePointOne = BigDecimal.valueOf(1.1D);
        assertParameter(shardingSpherePreparedStatement,1,onePointOne);
        assertParameter(shardingSpherePreparedStatement,2,onePointOne);
        assertParameter(shardingSpherePreparedStatement,3,onePointOne);
        assertParameter(shardingSpherePreparedStatement,4,one);
        assertParameter(shardingSpherePreparedStatement,5,one);
        assertParameter(shardingSpherePreparedStatement,6,one);
        assertParameter(shardingSpherePreparedStatement,7,BigDecimal.valueOf(date.getTime()));
        assertParameter(shardingSpherePreparedStatement,8,onePointOne);
        assertParameter(shardingSpherePreparedStatement,9,onePointOne);
        assertParameter(shardingSpherePreparedStatement,10,onePointOne);
        assertParameter(shardingSpherePreparedStatement,11,onePointOne);
        assertParameter(shardingSpherePreparedStatement,12,one);
        assertParameter(shardingSpherePreparedStatement,13,one);
        assertParameter(shardingSpherePreparedStatement,13,one);
        assertParameter(shardingSpherePreparedStatement,15,BigDecimal.valueOf(date.getTime()));
        assertParameter(shardingSpherePreparedStatement,16,onePointOne);
        assertParameter(shardingSpherePreparedStatement,17,one);
        assertParameter(shardingSpherePreparedStatement,18,one);
    }

    @Test
    public void assertSetObjectForDate(){
        java.util.Date date = new java.util.Date();
        long time = date.getTime();
        java.sql.Date sqlDate = new java.sql.Date(time);
        LocalDate localDate = LocalDate.now();
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        shardingSpherePreparedStatement.setObject(1,date,Types.DATE);
        shardingSpherePreparedStatement.setObject(2,time,Types.DATE);
        shardingSpherePreparedStatement.setObject(3,sqlDate,Types.DATE);
        shardingSpherePreparedStatement.setObject(4,localDate,Types.DATE);
        shardingSpherePreparedStatement.setObject(5,offsetDateTime,Types.DATE);
        shardingSpherePreparedStatement.setObject(6,zonedDateTime,Types.DATE);
        assertParameter(shardingSpherePreparedStatement,1,date);
        assertParameter(shardingSpherePreparedStatement,2,date);
        assertParameter(shardingSpherePreparedStatement,3,date);
        assertDateTimeParameter(shardingSpherePreparedStatement,4,date,1);
        assertDateTimeParameter(shardingSpherePreparedStatement,5,date,1);
        assertDateTimeParameter(shardingSpherePreparedStatement,6,date,1);
    }

    @Test
    public void assertSetObjectForTime(){
        java.util.Date date = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        LocalTime localTime = LocalTime.now();
        LocalDateTime localDateTime = LocalDateTime.now();
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        OffsetTime offsetTime = OffsetTime.now();
        shardingSpherePreparedStatement.setObject(1,date,Types.TIME);
        shardingSpherePreparedStatement.setObject(2,localTime,Types.TIME);
        shardingSpherePreparedStatement.setObject(3,localDateTime,Types.TIME);
        shardingSpherePreparedStatement.setObject(4,offsetDateTime,Types.TIME);
        shardingSpherePreparedStatement.setObject(5,zonedDateTime,Types.TIME);
        shardingSpherePreparedStatement.setObject(6,offsetTime,Types.TIME);
        shardingSpherePreparedStatement.setObject(7,sqlDate,Types.TIME);
        assertDateTimeParameter(shardingSpherePreparedStatement,1,date,2);
        assertDateTimeParameter(shardingSpherePreparedStatement,2,date,2);
        assertDateTimeParameter(shardingSpherePreparedStatement,3,date,2);
        assertDateTimeParameter(shardingSpherePreparedStatement,4,date,2);
        assertDateTimeParameter(shardingSpherePreparedStatement,5,date,2);
        assertDateTimeParameter(shardingSpherePreparedStatement,6,date,2);
        assertDateTimeParameter(shardingSpherePreparedStatement,7,date,2);
    }

    @Test
    public void assertSetObjectForTimeStamp(){
        java.util.Date date = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        LocalTime localTime = LocalTime.now();
        LocalDateTime localDateTime = LocalDateTime.now();
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        OffsetTime offsetTime = OffsetTime.now();
        shardingSpherePreparedStatement.setObject(1,date,Types.TIMESTAMP);
        shardingSpherePreparedStatement.setObject(2,sqlDate,Types.TIMESTAMP);
        shardingSpherePreparedStatement.setObject(3,localDateTime,Types.TIMESTAMP);
        shardingSpherePreparedStatement.setObject(4,offsetDateTime,Types.TIMESTAMP);
        shardingSpherePreparedStatement.setObject(5,zonedDateTime,Types.TIMESTAMP);
        shardingSpherePreparedStatement.setObject(6,localTime,Types.TIMESTAMP);
        shardingSpherePreparedStatement.setObject(7,offsetTime,Types.TIMESTAMP);
        assertDateTimeParameter(shardingSpherePreparedStatement,1,date,3);
        assertDateTimeParameter(shardingSpherePreparedStatement,2,date,3);
        assertDateTimeParameter(shardingSpherePreparedStatement,3,date,3);
        assertDateTimeParameter(shardingSpherePreparedStatement,4,date,3);
        assertDateTimeParameter(shardingSpherePreparedStatement,5,date,3);
        assertDateTimeParameter(shardingSpherePreparedStatement,6,date,3);
        assertDateTimeParameter(shardingSpherePreparedStatement,7,date,3);
    }


    @Test
    public void assertSetObjectForBoolean(){
        Boolean bool = new Boolean(true);
        int intBool = 1;
        char boolT = 't';
        String strBool = "true";
        String yesBool = "yes";

        Boolean bool_f = false;
        int intBoolF = 0;
        char boolF = 'f';
        String strBoolF = "false";
        String noBool = "no";

        shardingSpherePreparedStatement.setObject(1,bool,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(2,intBool,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(3,boolT,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(4,strBool,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(5,yesBool,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(6,bool_f,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(7,intBoolF,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(8,boolF,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(9,strBoolF,Types.BOOLEAN);
        shardingSpherePreparedStatement.setObject(10,noBool,Types.BOOLEAN);

        assertParameter(shardingSpherePreparedStatement,1,true);
        assertParameter(shardingSpherePreparedStatement,2,true);
        assertParameter(shardingSpherePreparedStatement,3,true);
        assertParameter(shardingSpherePreparedStatement,4,true);
        assertParameter(shardingSpherePreparedStatement,5,true);
        assertParameter(shardingSpherePreparedStatement,6,false);
        assertParameter(shardingSpherePreparedStatement,7,false);
        assertParameter(shardingSpherePreparedStatement,8,false);
        assertParameter(shardingSpherePreparedStatement,9,false);
        assertParameter(shardingSpherePreparedStatement,10,false);
    }

    private void assertDateTimeParameter(final PreparedStatement statement, final int index, final Object parameter, int level){
        Object obj = ((ShardingSpherePreparedStatement) statement).getParameters().get(index - 1);
        String value = obj.toString();
        String newVal = parameter.toString();
        if(level == 1){
            String date = parseDateFromToString(value);
            String newDate = parseDateFromToString(newVal);
            assertThat(date,equalTo(newDate));
        }else if(level == 2){
            String time = parseTimeFromToString(value);
            String newTime = parseTimeFromToString(newVal);
            assertThat(time,equalTo(newTime));
        }else if(level == 3){
            String datetime = parseDateFromToString(value) + parseTimeFromToString(value);
            String newDatetime = parseDateFromToString(newVal) + parseTimeFromToString(newVal);
            assertThat(datetime,equalTo(newDatetime));
        }
    }
    private String parseTimeFromToString(String value){
        String timeRex = "\\d{2}:\\d{2}:\\d{2}(.\\d{3})?(\\+\\d{2}:\\d{2})?(\\s([A-Za-z]+/?[A-Za-z]+))?";
        Pattern p = Pattern.compile(timeRex);
        Matcher matcher = p.matcher(value);
        if(matcher.find()){
            String syx = matcher.group();
            return syx.substring(0,8);

        }
        throw new AssertionError();
    }

    private String parseDateFromToString(String value){
        Map<String,String> monthMap = new HashMap<>();
        monthMap.put("Jan","01");
        monthMap.put("Feb","02");
        monthMap.put("Mar","03");
        monthMap.put("Apr","04");
        monthMap.put("May","05");
        monthMap.put("Jun","06");
        monthMap.put("Jul","07");
        monthMap.put("Aug","08");
        monthMap.put("Sep","09");
        monthMap.put("Oct","10");
        monthMap.put("Nov","11");
        monthMap.put("Dec","12");
        String result = null;
        String rex = "\\d{4}-\\d{2}-\\d{2}";
        //Tue Feb 22 14:42:42 CST 2022
        String another = "^[A-Z]{1}[a-z]{2} [A-Z]{1}[a-z]{2} \\d{2} \\d{2}:\\d{2}:\\d{2} [A-Z]{3} \\d{4}$";
        Pattern p = Pattern.compile(rex);
        Pattern ap = Pattern.compile(another);
        Matcher matcher = p.matcher(value);
        if(matcher.find()){
            result = matcher.group();
        }else{
            matcher = ap.matcher(value);
            if(matcher.find()){
                String dateStr = matcher.group();
                String[] dateArr = dateStr.split(" ");
                String month = monthMap.get(dateArr[1]);
                String day = dateArr[2];
                String year = dateArr[5];
                result = year+"-"+month+"-"+day;
            }else {
                throw new AssertionError();
            }
        }
        return result;
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
