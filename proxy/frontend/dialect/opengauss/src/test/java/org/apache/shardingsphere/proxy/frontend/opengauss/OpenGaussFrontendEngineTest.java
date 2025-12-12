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

import org.apache.shardingsphere.database.protocol.opengauss.codec.OpenGaussPacketCodecEngine;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.OpenGaussAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.OpenGaussCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenGaussFrontendEngineTest {
    
    private final OpenGaussFrontendEngine openGaussFrontendEngine = new OpenGaussFrontendEngine();
    
    @Mock
    private PostgreSQLFrontendEngine mockPostgreSQLFrontendEngine;
    
    @BeforeEach
    void setup() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(OpenGaussFrontendEngine.class.getDeclaredField("postgresqlFrontendEngine"), openGaussFrontendEngine, mockPostgreSQLFrontendEngine);
    }
    
    @Test
    void assertGetCommandExecuteEngine() {
        assertThat(openGaussFrontendEngine.getCommandExecuteEngine(), isA(OpenGaussCommandExecuteEngine.class));
    }
    
    @Test
    void assertGetCodecEngine() {
        assertThat(openGaussFrontendEngine.getCodecEngine(), isA(OpenGaussPacketCodecEngine.class));
    }
    
    @Test
    void assertGetAuthenticationEngine() {
        assertThat(openGaussFrontendEngine.getAuthenticationEngine(), isA(OpenGaussAuthenticationEngine.class));
    }
    
    @Test
    void assertRelease() {
        ConnectionSession connection = mock(ConnectionSession.class);
        openGaussFrontendEngine.release(connection);
        verify(mockPostgreSQLFrontendEngine).release(connection);
    }
}
