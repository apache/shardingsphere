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

package org.apache.shardingsphere.mcp.core.performance;

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.SearchMetadataToolHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPPerformanceBudgetSmokeTest {
    
    private static final long DESCRIPTOR_BUDGET_MILLIS = 5000L;
    
    private static final long REQUEST_SCOPE_BUDGET_MILLIS = 5000L;
    
    private static final long METADATA_SEARCH_BUDGET_MILLIS = 5000L;
    
    private static final long SQL_CLASSIFIER_BUDGET_MILLIS = 5000L;
    
    private static final int DESCRIPTOR_ITERATIONS = 100;
    
    private static final int REQUEST_SCOPE_ITERATIONS = 200;
    
    private static final int METADATA_SEARCH_ITERATIONS = 100;
    
    private static final int SQL_CLASSIFIER_ITERATIONS = 1000;
    
    @Test
    void assertDescriptorGenerationBudget() {
        final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
            final ServerCapabilitiesHandler handler = new ServerCapabilitiesHandler();
            final Map<String, Object> actual = handler.handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertTrue(actual.containsKey("fingerprints"));
            final long elapsedMillis = measureElapsedMillis(() -> {
                for (int i = 0; i < DESCRIPTOR_ITERATIONS; i++) {
                    handler.handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
                }
            });
            assertWithinBudget("descriptor generation", elapsedMillis, DESCRIPTOR_BUDGET_MILLIS);
        }
    }
    
    @Test
    void assertRequestScopeCreationBudget() {
        final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        final long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < REQUEST_SCOPE_ITERATIONS; i++) {
                try (MCPRequestScope ignored = new MCPRequestScope(runtimeContext)) {
                    ignored.getDatabaseContext();
                }
            }
        });
        assertWithinBudget("request scope creation", elapsedMillis, REQUEST_SCOPE_BUDGET_MILLIS);
    }
    
    @Test
    void assertMetadataSearchBudget() {
        final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
            final SearchMetadataToolHandler handler = new SearchMetadataToolHandler();
            final Map<String, Object> arguments = Map.of("query", "order", "object_types", List.of("table"));
            assertDoesNotThrow(() -> handler.handle(requestScope, new MCPToolCall("session-1", arguments)));
            final long elapsedMillis = measureElapsedMillis(() -> {
                for (int i = 0; i < METADATA_SEARCH_ITERATIONS; i++) {
                    handler.handle(requestScope, new MCPToolCall("session-1", arguments)).toPayload();
                }
            });
            assertWithinBudget("metadata search", elapsedMillis, METADATA_SEARCH_BUDGET_MILLIS);
        }
    }
    
    @Test
    void assertSQLClassifierBudget() {
        final StatementClassifier classifier = new StatementClassifier();
        assertDoesNotThrow(() -> classifier.classify("SELECT * FROM orders WHERE order_id = 1"));
        final long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < SQL_CLASSIFIER_ITERATIONS; i++) {
                classifier.classify("SELECT * FROM orders WHERE order_id = 1");
            }
        });
        assertWithinBudget("SQL classifier", elapsedMillis, SQL_CLASSIFIER_BUDGET_MILLIS);
    }
    
    private long measureElapsedMillis(final Runnable action) {
        final long startNanos = System.nanoTime();
        action.run();
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
    
    private void assertWithinBudget(final String name, final long elapsedMillis, final long budgetMillis) {
        assertTrue(elapsedMillis <= budgetMillis, () -> String.format("%s elapsedMillis=%d exceeded budgetMillis=%d", name, elapsedMillis, budgetMillis));
    }
}
