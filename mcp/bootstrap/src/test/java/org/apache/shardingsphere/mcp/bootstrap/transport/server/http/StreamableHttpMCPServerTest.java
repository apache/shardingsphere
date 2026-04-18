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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class StreamableHttpMCPServerTest {
    
    @Test
    void assertConstructWithRemoteHttpWithoutAccessToken() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new StreamableHttpMCPServer(new HttpTransportConfiguration(true, "0.0.0.0", true, "", 0, "/mcp"),
                        new MCPRuntimeContext(mock(MCPSessionManager.class), mock(MCPDatabaseCapabilityProvider.class))));
        assertThat(actual.getMessage(), is("Property `transport.http.accessToken` must not be blank when remote HTTP access is enabled."));
    }
}
