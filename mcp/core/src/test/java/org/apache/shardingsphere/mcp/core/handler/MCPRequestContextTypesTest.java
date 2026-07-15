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

package org.apache.shardingsphere.mcp.core.handler;

import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowRequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPRequestContextTypesTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSupportedContextTypes")
    void assertValidateContextType(final Class<?> contextType) {
        assertDoesNotThrow(() -> MCPRequestContextTypes.validateContextType(contextType, MCPRequestContextTypesTest.class));
    }
    
    private static Stream<Class<?>> getSupportedContextTypes() {
        return Stream.of(MCPRequestContext.class, MCPDatabaseRequestContext.class, MCPWorkflowRequestContext.class);
    }
    
    @Test
    void assertValidateUnsupportedContextType() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPRequestContextTypes.validateContextType(UnsupportedRequestContext.class, MCPRequestContextTypesTest.class));
        assertThat(actual.getMessage(), is(String.format("Unsupported request context type `%s` for `%s`.",
                UnsupportedRequestContext.class.getName(), MCPRequestContextTypesTest.class.getName())));
    }
    
    private interface UnsupportedRequestContext extends MCPRequestContext {
    }
}
