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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.MockShardingSphereSchemasUtil;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BroadcastBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Test
    public void assertExecuteSuccess() {
        MockShardingSphereSchemasUtil.setSchemas("schema", 10);
        mockDatabaseCommunicationEngine(new UpdateResponse());
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler("SET timeout = 1000", backendConnection);
        setBackendHandlerFactory(broadcastBackendHandler);
        BackendResponse actual = broadcastBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        assertThat(((UpdateResponse) actual).getUpdateCount(), is(0L));
        assertThat(((UpdateResponse) actual).getLastInsertId(), is(0L));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    @Test
    public void assertExecuteFailure() {
        MockShardingSphereSchemasUtil.setSchemas("schema", 10);
        ErrorResponse errorResponse = new ErrorResponse(new SQLException("no reason", "X999", -1));
        mockDatabaseCommunicationEngine(errorResponse);
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler("SET timeout = 1000", backendConnection);
        setBackendHandlerFactory(broadcastBackendHandler);
        assertThat(broadcastBackendHandler.execute(), instanceOf(ErrorResponse.class));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    private void mockDatabaseCommunicationEngine(final BackendResponse backendResponse) {
        when(databaseCommunicationEngine.execute()).thenReturn(backendResponse);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance(any(), anyString(), any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final BroadcastBackendHandler schemaBroadcastBackendHandler) {
        Field field = schemaBroadcastBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaBroadcastBackendHandler, databaseCommunicationEngineFactory);
    }
}
