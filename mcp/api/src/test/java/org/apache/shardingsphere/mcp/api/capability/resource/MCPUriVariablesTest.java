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

package org.apache.shardingsphere.mcp.api.capability.resource;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPUriVariablesTest {
    
    @Test
    void assertContainsVariable() {
        assertTrue(new MCPUriVariables(Map.of("foo_variable", "foo_value")).containsVariable("foo_variable"));
    }
    
    @Test
    void assertDoesNotContainVariable() {
        assertFalse(new MCPUriVariables(Map.of("foo_variable", "foo_value")).containsVariable("bar_variable"));
    }
    
    @Test
    void assertGetValueSuccess() {
        assertThat(new MCPUriVariables(Map.of("foo_variable", "foo_value")).getValue("foo_variable"), is("foo_value"));
    }
    
    @Test
    void assertGetValueFailedWithMissedVariable() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new MCPUriVariables(Collections.emptyMap()).getValue("foo_variable"));
        assertThat(ex.getMessage(), is("Missing URI variable `foo_variable`."));
    }
    
    @Test
    void assertGetValueFailedWithEmptyVariable() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new MCPUriVariables(Collections.singletonMap("foo_variable", "")).getValue("foo_variable"));
        assertThat(ex.getMessage(), is("Missing URI variable `foo_variable`."));
    }
}
