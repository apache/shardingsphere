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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTransportOriginUtilsTest {
    
    @Test
    void assertNormalizeOrigins() {
        assertThat(HttpTransportOriginUtils.normalizeOrigins(List.of(" HTTPS://Gateway.Example.Test ", "http://127.0.0.1:8080")),
                is(List.of("https://gateway.example.test", "http://127.0.0.1:8080")));
    }
    
    @Test
    void assertIsValidOrigin() {
        assertTrue(HttpTransportOriginUtils.isValidOrigin("https://gateway.example.test"));
    }
    
    @Test
    void assertIsValidOriginWithPath() {
        assertFalse(HttpTransportOriginUtils.isValidOrigin("https://gateway.example.test/path"));
    }
    
    @Test
    void assertNormalizeOrigin() {
        assertThat(HttpTransportOriginUtils.normalizeOrigin("HTTPS://Gateway.Example.Test:8443"), is("https://gateway.example.test:8443"));
    }
    
    @Test
    void assertNormalizeOriginWithIpv6Origin() {
        assertThat(HttpTransportOriginUtils.normalizeOrigin("http://[::1]:8080"), is("http://[::1]:8080"));
    }
    
    @Test
    void assertNormalizeOriginWithMalformedOrigin() {
        assertThat(HttpTransportOriginUtils.normalizeOrigin("://bad-origin"), is(""));
    }
}
