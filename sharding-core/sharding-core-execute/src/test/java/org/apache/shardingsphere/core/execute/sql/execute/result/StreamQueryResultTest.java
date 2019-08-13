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


package org.apache.shardingsphere.core.execute.sql.execute.result;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamQueryResultTest {

    @SneakyThrows
    private ResultSet getResultSet() {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false).thenReturn(true);
        doReturn(getResultSetMetaData()).when(resultSet).getMetaData();
        return resultSet;
    }

    @SneakyThrows
    private ResultSetMetaData getResultSetMetaData() {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("order_id");
        when(metaData.getColumnName(1)).thenReturn("order_id");
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(metaData.getTableName(1)).thenReturn("order");
        when(metaData.isCaseSensitive(1)).thenReturn(false);
        return metaData;
    }

    private ShardingEncryptor shardingEncryptor = mock(ShardingEncryptor.class);

    @Test
    public void assertConstructorWithShardingRule() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet(), getShardingRule());
        assertThat(queryResult.getQueryResultMetaData().getShardingEncryptor(1), is(Optional.fromNullable(shardingEncryptor)));
    }

    @Test
    public void assertConstructorWithEncryptRule() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet(), getEncryptRule());
        assertThat(queryResult.getQueryResultMetaData().getShardingEncryptor(1), is(Optional.fromNullable(shardingEncryptor)));
    }

    private ShardingRule getShardingRule() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        doReturn(getEncryptRule()).when(shardingRule).getEncryptRule();
        doReturn(Optional.fromNullable(getTableRule())).when(shardingRule).findTableRuleByActualTable("order");
        return shardingRule;
    }

    private TableRule getTableRule() {
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getLogicTable()).thenReturn("order");
        return tableRule;
    }

    private EncryptRule getEncryptRule() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.getShardingEncryptor("order", "order_id")).thenReturn(Optional.fromNullable(shardingEncryptor));
        when(encryptRule.isCipherColumn("order", "order_id")).thenReturn(false);
        return encryptRule;
    }

    @Test
    @SneakyThrows
    public void assertNext() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }

    @Test
    @SneakyThrows
    public void assertGetValueWithColumnIndex() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue(1, Integer.class), Is.<Object>is(1));
    }

    @Test
    @SneakyThrows
    public void assertGetValueWithColumnLabel() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is(1));
    }

    @Test
    @SneakyThrows
    public void assertGetValueWithShardingRule() {
        when(shardingEncryptor.decrypt("1")).thenReturn("1");
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet(), getShardingRule());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is("1"));
    }

    @Test(expected = Exception.class)
    @SneakyThrows
    public void assertGetValueWithException() {
        ResultSet resultSet = getResultSetWithException();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getValue("order_id", Integer.class);
    }

    @SneakyThrows
    private ResultSet getResultSetWithException() {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false);
        doReturn(getResultSetMetaDataWithException()).when(resultSet).getMetaData();
        return resultSet;
    }

    @SneakyThrows
    private ResultSetMetaData getResultSetMetaDataWithException() {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("order_id");
        when(metaData.getColumnName(1)).thenThrow(new SQLException());
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(metaData.getTableName(1)).thenReturn("order");
        return metaData;
    }

    @Test
    @SneakyThrows
    public void assertGetCalendarValueWithColumnIndexAndDate() {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Date.class, calendar);
        verify(resultSet).getDate(1, calendar);
    }

    @Test
    @SneakyThrows
    public void assertGetCalendarValueWithColumnIndexAndTime() {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Time.class, calendar);
        verify(resultSet).getTime(1, calendar);
    }

    @Test
    @SneakyThrows
    public void assertGetCalendarValueWithColumnIndexAndTimestamp() {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Timestamp.class, calendar);
        verify(resultSet).getTimestamp(1, calendar);
    }

    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertGetCalendarValueWithColumnIndexAndUnsupportedType() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }

    @Test
    @SneakyThrows
    public void assertGetCalendarValueWithColumnLabelAndDate() {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue("order_id", Date.class, calendar);
        verify(resultSet).getDate("order_id", calendar);
    }

    @Test
    @SneakyThrows
    public void assertGetCalendarValueWithColumnLabelAndTime() {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue("order_id", Time.class, calendar);
        verify(resultSet).getTime("order_id", calendar);
    }

    @Test
    @SneakyThrows
    public void assertGetCalendarValueWithColumnLabelAndTimestamp() {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue("order_id", Timestamp.class, calendar);
        verify(resultSet).getTimestamp("order_id", calendar);
    }

    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertGetCalendarValueWithColumnLabelAndUnsupportedType() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getCalendarValue("order_id", Object.class, Calendar.getInstance());
    }

    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnIndexAndAscii() {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Ascii");
        verify(resultSet).getAsciiStream(1);
    }

    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnIndexAndUnicode() {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Unicode");
        verify(resultSet).getUnicodeStream(1);
    }

    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnIndexAndBinary() {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Binary");
        verify(resultSet).getBinaryStream(1);
    }

    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertGetInputStreamWithColumnIndexAndUnsupportedType() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getInputStream(1, "Unsupported Type");
    }

    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnLabelAndAscii() {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream("order_id", "Ascii");
        verify(resultSet).getAsciiStream("order_id");
    }

    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnLabelAndUnicode() {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream("order_id", "Unicode");
        verify(resultSet).getUnicodeStream("order_id");
    }

    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnLabelAndBinary() {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream("order_id", "Binary");
        verify(resultSet).getBinaryStream("order_id");
    }

    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertGetInputStreamWithColumnLabelAndUnsupportedType() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getInputStream("order_id", "Unsupported Type");
    }

    @Test
    @SneakyThrows
    public void assertWasNull() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }

    @Test
    public void assertIsCaseSensitive() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertFalse(queryResult.isCaseSensitive(1));
    }

    @Test
    public void assertGetColumnCount() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertThat(queryResult.getColumnCount(), Is.is(1));
    }

    @Test
    public void assertGetColumnLabel() {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertThat(queryResult.getColumnLabel(1), Is.is("order_id"));
    }
}
