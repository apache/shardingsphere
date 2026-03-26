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

package org.apache.shardingsphere.test.e2e.mcp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeAuditE2ETest extends AbstractMCPE2ETest {
    
    @Test
    void assertAuditCaptureAndRefreshVisibility() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "CREATE TABLE orders_archive"));
        
        assertThat(actual.statusCode(), is(200));
        assertFalse(getRuntime().getRuntimeServices().getAuditRecorder().snapshot().isEmpty());
        assertTrue(getRuntime().getRuntimeServices().getMetadataRefreshCoordinator().isVisibleToSession(sessionId, "logic_db"));
    }
    
    @Test
    void assertInitializeAnonymousSession() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        
        String sessionId = initializeSession(httpClient);
        
        assertTrue(getRuntime().getSessionManager().hasSession(sessionId));
        assertThat(getRuntime().getSessionManager().findSession(sessionId).orElseThrow().getSessionId(), is(sessionId));
    }
    
    @Test
    void assertInitializeAnonymousSessionWithoutProtocolVersion() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        
        String sessionId = initializeSessionWithoutProtocolVersion(httpClient);
        
        assertTrue(getRuntime().getSessionManager().hasSession(sessionId));
        assertThat(getRuntime().getSessionManager().findSession(sessionId).orElseThrow().getSessionId(), is(sessionId));
    }
}
