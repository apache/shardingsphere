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

package org.apache.shardingsphere.shardingproxy.frontend.mysql.command.query.binary.execute;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MySQLComStmtExecuteExecutorTest {
    
    @Mock
    private SQLException sqlException;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Test
    @SneakyThrows
    public void assertIsErrorResponse() {
        MySQLComStmtExecuteExecutor mySQLComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(mock(MySQLComStmtExecutePacket.class), mock(BackendConnection.class));
        FieldSetter.setField(mySQLComStmtExecuteExecutor, MySQLComStmtExecuteExecutor.class.getDeclaredField("databaseCommunicationEngine"), databaseCommunicationEngine);
        when(sqlException.getCause()).thenReturn(new Exception());
        when(databaseCommunicationEngine.execute()).thenReturn(new ErrorResponse(sqlException));
        mySQLComStmtExecuteExecutor.execute();
        Assert.assertThat(mySQLComStmtExecuteExecutor.isErrorResponse(), Matchers.is(true));
    }
}
