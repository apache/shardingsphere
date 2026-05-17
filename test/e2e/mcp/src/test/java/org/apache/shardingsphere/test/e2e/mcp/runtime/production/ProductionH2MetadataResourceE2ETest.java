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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class ProductionH2MetadataResourceE2ETest extends AbstractProductionH2RuntimeE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadSingleMetadataResourceCases")
    void assertReadSingleMetadataResource(final String name, final RuntimeTransport transport,
                                          final String resourceUri, final String key, final String expectedValue) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(resourceUri), key, expectedValue);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadCollectionMetadataResourceCases")
    void assertReadCollectionMetadataResource(final String name, final RuntimeTransport transport,
                                              final String resourceUri, final String key, final List<String> expectedNames) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertItemValues(interactionClient.readResource(resourceUri), key, expectedNames);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertTableDetailResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualItem = MCPPayloadAssertions.getSingleItem(
                    interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
            assertThat(String.valueOf(actualItem.get("table")), is("orders"));
            assertThat(getNestedNames(actualItem, "columns", "column"), is(List.of("amount", "order_id", "status")));
            assertThat(getNestedNames(actualItem, "indexes", "index"), is(List.of("PRIMARY_KEY_C", "idx_orders_status")));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadViewsResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/views"), "view", "active_orders");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadIndexesResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertItemValues(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes"), "index",
                    List.of("PRIMARY_KEY_C", "idx_orders_status"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadSequencesResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource("shardingsphere://databases/logic_db/schemas/public/sequences"), "sequence", "order_seq");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertSearchSequence(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("sequence"))), "name", "order_seq");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertSearchTableAndViewMetadata(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertItemValues(interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("table", "view"))), "name",
                    List.of("order_items", "orders", "active_orders"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectUnsupportedObjectType(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "order",
                            "object_types", List.of("table", "view", "index", "materialized_view", "sequence")));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("object_types[3] must be one of [database, schema, table, view, column, index, sequence]."));
        }
    }
}
