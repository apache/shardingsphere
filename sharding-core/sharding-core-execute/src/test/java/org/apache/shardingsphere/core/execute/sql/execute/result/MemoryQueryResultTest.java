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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MemoryQueryResultTest {
    
    private ShardingEncryptor shardingEncryptor = mock(ShardingEncryptor.class);
    
    @Test
    public void assertConstructorWithShardingRule() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet(), getShardingRule());
        assertThat(queryResult.getQueryResultMetaData().getShardingEncryptor(1), is(Optional.fromNullable(shardingEncryptor)));
    }
    
    @Test
    public void assertConstructorWithEncryptRule() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet(), getEncryptRule());
        assertThat(queryResult.getQueryResultMetaData().getShardingEncryptor(1), is(Optional.fromNullable(shardingEncryptor)));
    }
    
    @Test(expected = SQLException.class)
    public void assertConstructorWithSqlException() throws SQLException {
        ResultSet resultSet = getResultSet();
        when(resultSet.next()).thenThrow(new SQLException());
        new MemoryQueryResult(resultSet);
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
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue(1, Integer.class), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetValueWithShardingRule() throws SQLException {
        when(shardingEncryptor.decrypt("1")).thenReturn("1");
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet(), getShardingRule());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is("1"));
    }
    
    @Test(expected = Exception.class)
    public void assertGetValueWithException() throws SQLException {
        ResultSet result = getResultSetWithException();
        MemoryQueryResult queryResult = new MemoryQueryResult(result);
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
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getCalendarValue(1, Integer.class, Calendar.getInstance()), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabel() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getCalendarValue("order_id", Integer.class, Calendar.getInstance()), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndex() throws SQLException, IOException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        InputStream inputStream = queryResult.getInputStream(1, "Unicode");
        assertThat(inputStream.read(), is(getInputStream(1).read()));
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabel() throws SQLException, IOException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        InputStream inputStream = queryResult.getInputStream("order_id", "Unicode");
        assertThat(inputStream.read(), is(getInputStream(1).read()));
    }
    
    private InputStream getInputStream(final Object value) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(result);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(result.toByteArray());
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertFalse(queryResult.isCaseSensitive(1));
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertThat(queryResult.getColumnCount(), is(1));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertThat(queryResult.getColumnLabel(1), is("order_id"));
    }
    
    private ResultSet getResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true).thenReturn(false);
        when(result.getInt(1)).thenReturn(1);
        when(result.wasNull()).thenReturn(false);
        doReturn(getResultSetMetaData()).when(result).getMetaData();
        return result;
    }
    
    private ResultSetMetaData getResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.getTableName(1)).thenReturn("order");
        when(result.isCaseSensitive(1)).thenReturn(false);
        return result;
    }
}
