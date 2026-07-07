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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ProductionRuntimeTransportCases {
    
    static Stream<Arguments> transports() {
        return runtimeTransports().map(each -> Arguments.of(getTransportName(each), each));
    }
    
    static Stream<Arguments> semanticPrimaryTransport() {
        return semanticPrimaryRuntimeTransports().map(each -> Arguments.of(getTransportName(each), each));
    }
    
    static Stream<Arguments> assertReadSingleMetadataResourceCases(final String logicalDatabaseName) {
        return semanticPrimaryRuntimeTransports().flatMap(each -> createSingleMetadataResourceCases(logicalDatabaseName, each));
    }
    
    static Stream<Arguments> assertReadCollectionMetadataResourceCases(final String logicalDatabaseName) {
        return semanticPrimaryRuntimeTransports().flatMap(each -> Stream.of(
                Arguments.of(getTransportName(each) + " schemas list", each, "shardingsphere://databases/" + logicalDatabaseName + "/schemas", "schema", List.of(logicalDatabaseName)),
                Arguments.of(getTransportName(each) + " tables list", each,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables", "table", List.of("order_items", "orders")),
                Arguments.of(getTransportName(each) + " table columns list", each,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables/orders/columns", "column", List.of("amount", "order_id", "status")),
                Arguments.of(getTransportName(each) + " view columns list", each,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/views/active_orders/columns", "column", List.of("order_id", "status"))));
    }
    
    static Stream<RuntimeTransport> runtimeTransports() {
        return Stream.of(RuntimeTransport.HTTP, RuntimeTransport.STDIO);
    }
    
    static String getTransportName(final RuntimeTransport transport) {
        return RuntimeTransport.HTTP == transport ? "http" : "stdio";
    }
    
    private static Stream<RuntimeTransport> semanticPrimaryRuntimeTransports() {
        return Stream.of(RuntimeTransport.HTTP);
    }
    
    private static Stream<Arguments> createSingleMetadataResourceCases(final String logicalDatabaseName, final RuntimeTransport transport) {
        return Stream.of(
                Arguments.of(getTransportName(transport) + " database detail", transport, "shardingsphere://databases/" + logicalDatabaseName, "database", logicalDatabaseName),
                Arguments.of(getTransportName(transport) + " schema detail", transport, "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName, "schema",
                        logicalDatabaseName),
                Arguments.of(getTransportName(transport) + " table column detail", transport,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables/orders/columns/status", "column", "status"),
                Arguments.of(getTransportName(transport) + " view detail", transport,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/views/active_orders", "view", "active_orders"),
                Arguments.of(getTransportName(transport) + " view column detail", transport,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/views/active_orders/columns/status", "column", "status"),
                Arguments.of(getTransportName(transport) + " index detail", transport,
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables/orders/indexes/idx_orders_status", "index", "idx_orders_status"));
    }
}
