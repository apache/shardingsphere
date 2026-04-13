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

package org.apache.shardingsphere.mcp.bootstrap.config;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPLaunchConfigurationTest {
    
    @Test
    void assertValidateWhenHttpTransportEnabled() {
        assertDoesNotThrow(createLaunchConfiguration(true, false)::validate);
    }
    
    @Test
    void assertValidateWhenStdioTransportEnabled() {
        assertDoesNotThrow(createLaunchConfiguration(false, true)::validate);
    }
    
    @Test
    void assertValidateWhenBothTransportsEnabled() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(true, true)::validate);
        assertThat(actual.getMessage(), is("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."));
    }
    
    @Test
    void assertValidateWhenBothTransportsDisabled() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(false, false)::validate);
        assertThat(actual.getMessage(), is("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    @Test
    void assertValidateWhenRemoteHttpIsNotExplicitlyAllowed() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(true, false, "0.0.0.0", false, "")::validate);
        assertThat(actual.getMessage(), is("Property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback."));
    }
    
    @Test
    void assertValidateWhenRemoteHttpAccessTokenIsMissing() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(true, false, "0.0.0.0", true, "")::validate);
        assertThat(actual.getMessage(), is("Property `transport.http.accessToken` must not be blank when remote HTTP access is enabled."));
    }
    
    @Test
    void assertValidateWhenDisabledRemoteHttpDoesNotRequireAccessToken() {
        assertDoesNotThrow(createLaunchConfiguration(false, true, "0.0.0.0", false, "")::validate);
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final boolean stdioEnabled) {
        return createLaunchConfiguration(httpEnabled, stdioEnabled, "127.0.0.1", false, "");
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final boolean stdioEnabled,
                                                             final String bindHost, final boolean allowRemoteAccess, final String accessToken) {
        return new MCPLaunchConfiguration(new HttpTransportConfiguration(httpEnabled, bindHost, allowRemoteAccess, accessToken, 0, "/mcp"),
                new StdioTransportConfiguration(stdioEnabled), Collections.emptyMap());
    }
}
