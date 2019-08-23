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
public final class StreamQueryResultTest {
    
    private final ShardingEncryptor shardingEncryptor = mock(ShardingEncryptor.class);

    @Test
    public void assertConstructorWithShardingRule() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet(), getShardingRule());
        assertThat(queryResult.getQueryResultMetaData().getShardingEncryptor(1), is(Optional.fromNullable(shardingEncryptor)));
    }
    
    @Test
    public void assertConstructorWithEncryptRule() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet(), getEncryptRule());
        assertThat(queryResult.getQueryResultMetaData().getShardingEncryptor(1), is(Optional.fromNullable(shardingEncryptor)));
    }
    
    private ShardingRule getShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        doReturn(getEncryptRule()).when(result).getEncryptRule();
        doReturn(Optional.fromNullable(getTableRule())).when(result).findTableRuleByActualTable("order");
        return result;
    }
    
    private TableRule getTableRule() {
        TableRule result = mock(TableRule.class);
        when(result.getLogicTable()).thenReturn("order");
        return result;
    }
    
    private EncryptRule getEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        when(result.getShardingEncryptor("order", "order_id")).thenReturn(Optional.fromNullable(shardingEncryptor));
        when(result.isCipherColumn("order", "order_id")).thenReturn(false);
        return result;
    }
    
    @Test
    public void assertNext() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue(1, Integer.class), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetValueWithShardingRule() throws SQLException {
        when(shardingEncryptor.decrypt("1")).thenReturn("1");
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet(), getShardingRule());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is("1"));
    }
    
    @Test(expected = Exception.class)
    public void assertGetValueWithException() throws SQLException {
        ResultSet resultSet = getResultSetWithException();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getValue("order_id", Integer.class);
    }
    
    private ResultSet getResultSetWithException() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true).thenReturn(false);
        when(result.getInt(1)).thenReturn(1);
        when(result.wasNull()).thenReturn(false);
        doReturn(getResultSetMetaDataWithException()).when(result).getMetaData();
        return result;
    }
    
    private ResultSetMetaData getResultSetMetaDataWithException() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenThrow(new SQLException());
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.getTableName(1)).thenReturn("order");
        return result;
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndexAndDate() throws SQLException {
        ResultSet result = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(result);
        queryResult.next();
        queryResult.getCalendarValue(1, Date.class, calendar);
        verify(result).getDate(1, calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndexAndTime() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Time.class, calendar);
        verify(resultSet).getTime(1, calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndexAndTimestamp() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Timestamp.class, calendar);
        verify(resultSet).getTimestamp(1, calendar);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCalendarValueWithColumnIndexAndUnsupportedType() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabelAndDate() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue("order_id", Date.class, calendar);
        verify(resultSet).getDate("order_id", calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabelAndTime() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue("order_id", Time.class, calendar);
        verify(resultSet).getTime("order_id", calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabelAndTimestamp() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue("order_id", Timestamp.class, calendar);
        verify(resultSet).getTimestamp("order_id", calendar);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCalendarValueWithColumnLabelAndUnsupportedType() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getCalendarValue("order_id", Object.class, Calendar.getInstance());
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndexAndAscii() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Ascii");
        verify(resultSet).getAsciiStream(1);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetInputStreamWithColumnIndexAndUnicode() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Unicode");
        verify(resultSet).getUnicodeStream(1);
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndexAndBinary() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Binary");
        verify(resultSet).getBinaryStream(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetInputStreamWithColumnIndexAndUnsupportedType() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getInputStream(1, "Unsupported Type");
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabelAndAscii() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream("order_id", "Ascii");
        verify(resultSet).getAsciiStream("order_id");
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetInputStreamWithColumnLabelAndUnicode() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream("order_id", "Unicode");
        verify(resultSet).getUnicodeStream("order_id");
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabelAndBinary() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream("order_id", "Binary");
        verify(resultSet).getBinaryStream("order_id");
    }
    
    @Test(expected = SQLException.class)
    public void assertGetInputStreamWithColumnLabelAndUnsupportedType() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getInputStream("order_id", "Unsupported Type");
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertFalse(queryResult.isCaseSensitive(1));
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertThat(queryResult.getColumnCount(), Is.is(1));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertThat(queryResult.getColumnLabel(1), Is.is("order_id"));
    }
    
    private ResultSet getResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false).thenReturn(true);
        doReturn(getResultSetMetaData()).when(resultSet).getMetaData();
        return resultSet;
    }
    
    private ResultSetMetaData getResultSetMetaData() throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("order_id");
        when(metaData.getColumnName(1)).thenReturn("order_id");
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(metaData.getTableName(1)).thenReturn("order");
        when(metaData.isCaseSensitive(1)).thenReturn(false);
        return metaData;
    }
}
