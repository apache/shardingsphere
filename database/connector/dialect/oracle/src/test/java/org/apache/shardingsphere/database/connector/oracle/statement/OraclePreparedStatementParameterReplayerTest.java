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

package org.apache.shardingsphere.database.connector.oracle.statement;

import org.apache.shardingsphere.database.connector.core.statement.PreparedStatementParameter;
import org.apache.shardingsphere.database.connector.core.statement.SetterMethodType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OraclePreparedStatementParameterReplayerTest {
    
    private final OraclePreparedStatementParameterReplayer replayer = new OraclePreparedStatementParameterReplayer();
    
    @Test
    void assertReplayWithSetObject() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_OBJECT, "testValue", -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setObject(1, "testValue");
    }
    
    @Test
    void assertReplayWithSetNull() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_NULL, null, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setObject(1, null);
    }
    
    @Test
    void assertReplayWithBinaryStreamNoLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BINARY_STREAM, inputStream, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setBinaryStream(1, inputStream);
    }
    
    @Test
    void assertReplayWithBinaryStreamWithLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BINARY_STREAM, inputStream, 100L);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setBinaryStream(1, inputStream, 100L);
    }
    
    @Test
    void assertReplayWithBlobInputStreamNoLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BLOB_INPUT_STREAM, inputStream, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setBlob(1, inputStream);
    }
    
    @Test
    void assertReplayWithBlobInputStreamWithLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BLOB_INPUT_STREAM, inputStream, 100L);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setBlob(1, inputStream, 100L);
    }
    
    @Test
    void assertReplayWithBytes() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        byte[] bytes = new byte[]{1, 2, 3};
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BYTES, bytes, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setBytes(1, bytes);
    }
    
    @Test
    void assertReplayWithAsciiStreamNoLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_ASCII_STREAM, inputStream, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setAsciiStream(1, inputStream);
    }
    
    @Test
    void assertReplayWithAsciiStreamWithLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_ASCII_STREAM, inputStream, 100L);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setAsciiStream(1, inputStream, 100L);
    }
    
    @Test
    void assertReplayWithCharacterStreamNoLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        StringReader reader = new StringReader("test");
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_CHARACTER_STREAM, reader, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setCharacterStream(1, reader);
    }
    
    @Test
    void assertReplayWithCharacterStreamWithLength() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        StringReader reader = new StringReader("test");
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_CHARACTER_STREAM, reader, 100L);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setCharacterStream(1, reader, 100L);
    }
    
    @Test
    void assertGetDatabaseType() {
        assertThat(replayer.getDatabaseType(), is("Oracle"));
    }
}
