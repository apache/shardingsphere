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

import jakarta.servlet.http.HttpServletRequest;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionAttributionResolverTest {
    
    @Test
    void assertResolveFromRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Test-Subject")).thenReturn("subject");
        when(request.getHeader("X-Test-Source")).thenReturn("gateway");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("X-Test-Subject", "X-Test-Source", "X-Test-ATTR-Region")));
        when(request.getHeader("X-Test-ATTR-Region")).thenReturn("ap-south");
        SessionAttributionResolver resolver = new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-"));
        assertSessionIdentity(resolver.resolve(request, "session-1"), "subject", "gateway", Map.of("region", "ap-south"));
    }
    
    @Test
    void assertResolveFromHeaders() {
        SessionAttributionResolver resolver = new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-"));
        assertSessionIdentity(resolver.resolve(Map.of("X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("gateway"), "x-test-attr-Region", List.of("ap-south")), "session-1"),
                "subject", "gateway", Map.of("region", "ap-south"));
    }
    
    @Test
    void assertResolveUnattributedWithoutSubject() {
        SessionAttributionResolver resolver = new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-"));
        assertSessionIdentity(resolver.resolve(Map.of(), "session-1"), "", "", Map.of());
    }
    
    @Test
    void assertResolveUnattributedWhenDisabled() {
        assertSessionIdentity(new SessionAttributionResolver(null).resolve(Map.of(), "session-1"), "", "", Map.of());
    }
    
    private void assertSessionIdentity(final MCPSessionIdentity actual, final String subject, final String source, final Map<String, String> attributes) {
        assertThat(actual.getSessionId(), is("session-1"));
        assertThat(actual.getSubject(), is(subject));
        assertThat(actual.getSource(), is(source));
        assertThat(actual.getAttributes(), is(attributes));
    }
}
