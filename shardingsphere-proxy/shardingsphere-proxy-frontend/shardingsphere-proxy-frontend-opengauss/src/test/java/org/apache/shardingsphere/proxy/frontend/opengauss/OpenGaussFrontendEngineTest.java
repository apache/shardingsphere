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

import org.apache.shardingsphere.db.protocol.opengauss.codec.OpenGaussPacketCodecEngine;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.OpenGaussAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.OpenGaussCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussFrontendEngineTest {
    
    private final OpenGaussFrontendEngine openGaussFrontendEngine = new OpenGaussFrontendEngine();
    
    @Mock
    private PostgreSQLFrontendEngine mockPostgreSQLFrontendEngine;
    
    @Before
    public void setup() throws ReflectiveOperationException {
        Field field = OpenGaussFrontendEngine.class.getDeclaredField("postgreSQLFrontendEngine");
        field.setAccessible(true);
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
        assertThat(openGaussFrontendEngine.getCodecEngine(), instanceOf(OpenGaussPacketCodecEngine.class));
    }
    
    @Test
    public void assertGetAuthenticationEngine() {
        assertThat(openGaussFrontendEngine.getAuthenticationEngine(), instanceOf(OpenGaussAuthenticationEngine.class));
    }
    
    @Test
    public void assertRelease() {
        ConnectionSession connection = mock(ConnectionSession.class);
        openGaussFrontendEngine.release(connection);
        verify(mockPostgreSQLFrontendEngine).release(connection);
    }
}
