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

package org.apache.shardingsphere.database.connector.core.statement;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultPreparedStatementParameterReplayerTest {
    
    private final DefaultPreparedStatementParameterReplayer replayer = new DefaultPreparedStatementParameterReplayer();
    
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
    void assertReplayWithInputStream() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BINARY_STREAM, inputStream, -1);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setObject(1, inputStream);
    }
    
    @Test
    void assertReplayWithBlobInputStream() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        PreparedStatementParameter parameter = new PreparedStatementParameter(1, SetterMethodType.SET_BLOB_INPUT_STREAM, inputStream, 3);
        
        replayer.replay(preparedStatement, parameter);
        
        verify(preparedStatement).setObject(1, inputStream);
    }
    
    @Test
    void assertGetDatabaseType() {
        assertThat(replayer.getDatabaseType(), is(nullValue()));
    }
}
