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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPTransportConfigurationTest {
    
    @Test
    void assertValidate() {
        assertDoesNotThrow(() -> createTransportConfiguration(true, false).validate());
    }
    
    @Test
    void assertValidateWithMultipleEnabledTransports() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> createTransportConfiguration(true, true).validate());
        
        assertThat(actual.getMessage(), is("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."));
    }
    
    @Test
    void assertValidateWithDisabledTransports() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> createTransportConfiguration(false, false).validate());
        
        assertThat(actual.getMessage(), is("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    private MCPTransportConfiguration createTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled) {
        return new MCPTransportConfiguration(new HttpTransportConfiguration(httpEnabled, "127.0.0.1", 0, "/mcp"), new StdioTransportConfiguration(stdioEnabled));
    }
}
