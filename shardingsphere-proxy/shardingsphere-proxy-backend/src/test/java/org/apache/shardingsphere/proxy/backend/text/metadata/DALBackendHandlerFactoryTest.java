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

package org.apache.shardingsphere.proxy.backend.text.metadata;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.BroadcastDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.UnicastDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class DALBackendHandlerFactoryTest {
    
    @Test
    public void assertBroadcastBackendHandlerReturnedWhenSetStatement() {
        SetStatement setStatement = mock(SetStatement.class);
        BackendConnection backendConnection = mock(BackendConnection.class);
        TextProtocolBackendHandler textProtocolBackendHandler = DALBackendHandlerFactory.newInstance(setStatement, "", backendConnection);
        assertThat(textProtocolBackendHandler, instanceOf(BroadcastDatabaseBackendHandler.class));
        BroadcastDatabaseBackendHandler broadcastBackendHandler = (BroadcastDatabaseBackendHandler) textProtocolBackendHandler;
        assertFieldOfInstance(broadcastBackendHandler, "sqlStatement", is(setStatement));
        assertFieldOfInstance(broadcastBackendHandler, "sql", is(""));
        assertFieldOfInstance(broadcastBackendHandler, "backendConnection", is(backendConnection));
    }
    
    @Test
    public void assertDatabaseBackendHandlerReturnedWhenOtherDALStatement() {
        DALStatement dalStatement = mock(DALStatement.class);
        BackendConnection backendConnection = mock(BackendConnection.class);
        TextProtocolBackendHandler textProtocolBackendHandler = DALBackendHandlerFactory.newInstance(dalStatement, "", backendConnection);
        assertThat(textProtocolBackendHandler, instanceOf(UnicastDatabaseBackendHandler.class));
        UnicastDatabaseBackendHandler backendHandler = (UnicastDatabaseBackendHandler) textProtocolBackendHandler;
        assertFieldOfInstance(backendHandler, "sqlStatement", is(dalStatement));
        assertFieldOfInstance(backendHandler, "sql", is(""));
        assertFieldOfInstance(backendHandler, "backendConnection", is(backendConnection));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <S, T> void assertFieldOfInstance(final S classInstance, final String fieldName, final Matcher<T> matcher) {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        T value = (T) field.get(classInstance);
        assertThat(value, matcher);
    }
}
