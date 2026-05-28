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
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        when(request.getHeaderNames()).thenReturn(java.util.Collections.enumeration(List.of("X-Test-Subject", "X-Test-Source", "X-Test-Attr-region")));
        when(request.getHeader("X-Test-Attr-region")).thenReturn("ap-south");
        SessionAttributionResolver resolver = new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-"));
        assertThat(resolver.resolve(request).map(each -> each.getAttributes().get("region")), is(Optional.of("ap-south")));
    }
    
    @Test
    void assertResolveFromHeaders() {
        SessionAttributionResolver resolver = new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-"));
        assertThat(resolver.resolve(Map.of("X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("gateway"))).get().getSubject(), is("subject"));
    }
}
