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

package org.apache.shardingsphere.proxy.frontend.opengauss;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.OpenGaussCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussFrontendEngineTest {
    
    private OpenGaussFrontendEngine openGaussFrontendEngine;
    
    @Mock
    private PostgreSQLFrontendEngine mockPostgreSQLFrontendEngine;
    
    @Before
    public void setup() {
        openGaussFrontendEngine = new OpenGaussFrontendEngine();
        prepareMock();
    }
    
    @SneakyThrows
    private void prepareMock() {
        Field field = OpenGaussFrontendEngine.class.getDeclaredField("postgreSQLFrontendEngine");
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(openGaussFrontendEngine, mockPostgreSQLFrontendEngine);
    }
    
    @Test
    public void assertGetCommandExecuteEngine() {
        assertThat(openGaussFrontendEngine.getCommandExecuteEngine(), instanceOf(OpenGaussCommandExecuteEngine.class));
    }
    
    @Test
    public void assertGetFrontendContext() {
        openGaussFrontendEngine.getFrontendContext();
        verify(mockPostgreSQLFrontendEngine).getFrontendContext();
    }
    
    @Test
    public void assertGetCodecEngine() {
        openGaussFrontendEngine.getCodecEngine();
        verify(mockPostgreSQLFrontendEngine).getCodecEngine();
    }
    
    @Test
    public void assertGetAuthenticationEngine() {
        openGaussFrontendEngine.getAuthenticationEngine();
        verify(mockPostgreSQLFrontendEngine).getAuthenticationEngine();
    }
    
    @Test
    public void assertRelease() {
        BackendConnection connection = mock(BackendConnection.class);
        openGaussFrontendEngine.release(connection);
        verify(mockPostgreSQLFrontendEngine).release(connection);
    }
    
    @Test
    public void assertGetDatabaseType() {
        assertThat(openGaussFrontendEngine.getDatabaseType(), is("openGauss"));
    }
}
