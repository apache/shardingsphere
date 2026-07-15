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

package org.apache.shardingsphere.mcp.support.protocol.payload;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPItemsPayloadTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToPayloadCases")
    void assertToPayload(final String name, final MCPItemsPayload response, final Map<String, Object> expectedPayload) {
        Map<String, Object> actual = response.toPayload();
        assertThat(actual, is(expectedPayload));
    }
    
    private static Stream<Arguments> assertToPayloadCases() {
        return Stream.of(
                Arguments.of("without page token", new MCPItemsPayload(List.of("foo_item")),
                        Map.of("response_mode", "list", "items", List.of("foo_item"), "count", 1, "has_more", false, "continuation_mode", "none")),
                Arguments.of("with null page token", new MCPItemsPayload(List.of("foo_item"), (String) null),
                        Map.of("response_mode", "list", "items", List.of("foo_item"), "count", 1, "has_more", false, "continuation_mode", "none")),
                Arguments.of("with page token", new MCPItemsPayload(List.of("foo_item"), "foo_token"),
                        Map.of("response_mode", "list", "items", List.of("foo_item"), "count", 1, "has_more", true, "next_page_token", "foo_token",
                                "continuation_mode", "pagination")),
                Arguments.of("with null items", new MCPItemsPayload(null),
                        Map.of("response_mode", "list", "items", List.of(), "count", 0, "has_more", false, "continuation_mode", "none")),
                Arguments.of("with navigation", new MCPItemsPayload(List.of("foo_item"), Map.of("self_uri", "shardingsphere://foo", "next_resources",
                        List.of(Map.of("uri", "shardingsphere://foo/bar", "resource_kind", "resource", "purpose", "inspect_detail", "reason", "Read child.",
                                "source_field", "next_resources")))),
                        Map.of("response_mode", "list", "items", List.of("foo_item"), "count", 1, "has_more", false, "continuation_mode", "none", "self_uri", "shardingsphere://foo",
                                "next_resources", List.of(Map.of("uri", "shardingsphere://foo/bar", "resource_kind", "resource", "purpose", "inspect_detail",
                                        "reason", "Read child.", "source_field", "next_resources")))));
    }
}
