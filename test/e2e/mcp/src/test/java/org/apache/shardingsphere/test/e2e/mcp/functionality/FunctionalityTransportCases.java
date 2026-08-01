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

package org.apache.shardingsphere.test.e2e.mcp.functionality;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FunctionalityTransportCases {
    
    static Stream<Arguments> allTransportCases() {
        return allTransports().map(each -> Arguments.of(getTransportName(each), each));
    }
    
    static Stream<Arguments> singleMetadataResourceCases(final String logicalDatabaseName) {
        return Stream.of(
                Arguments.of("database detail", "shardingsphere://databases/" + logicalDatabaseName, "database", logicalDatabaseName),
                Arguments.of("schema detail", "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName, "schema", logicalDatabaseName),
                Arguments.of("table column detail",
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables/orders/columns/status", "column", "status"),
                Arguments.of("view detail", "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/views/active_orders", "view", "active_orders"),
                Arguments.of("view column detail",
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/views/active_orders/columns/status", "column", "status"),
                Arguments.of("index detail",
                        "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables/orders/indexes/idx_orders_status", "index", "idx_orders_status"));
    }
    
    static Stream<Arguments> collectionMetadataResourceCases(final String logicalDatabaseName) {
        return Stream.of(
                Arguments.of("schemas list", "shardingsphere://databases/" + logicalDatabaseName + "/schemas", "schema", List.of(logicalDatabaseName)),
                Arguments.of("tables list", "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables", "table", List.of("order_items", "orders")),
                Arguments.of("table columns list", "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/tables/orders/columns", "column",
                        List.of("amount", "order_id", "status")),
                Arguments.of("view columns list", "shardingsphere://databases/" + logicalDatabaseName + "/schemas/" + logicalDatabaseName + "/views/active_orders/columns", "column",
                        List.of("order_id", "status")));
    }
    
    private static Stream<RuntimeTransport> allTransports() {
        return Stream.of(RuntimeTransport.HTTP, RuntimeTransport.STDIO);
    }
    
    private static String getTransportName(final RuntimeTransport transport) {
        return RuntimeTransport.HTTP == transport ? "http" : "stdio";
    }
    
}
