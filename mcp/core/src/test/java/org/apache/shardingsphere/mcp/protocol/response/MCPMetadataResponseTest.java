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

package org.apache.shardingsphere.mcp.protocol.response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPMetadataResponseTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToPayloadCases")
    void assertToPayload(final String name, final MCPMetadataResponse response, final Map<String, Object> expectedPayload) {
        Map<String, Object> actual = response.toPayload();
        assertThat(actual, is(expectedPayload));
    }
    
    private static Stream<Arguments> assertToPayloadCases() {
        return Stream.of(
                Arguments.of("without page token", new MCPMetadataResponse(List.of("foo_item")), Map.of("items", List.of("foo_item"))),
                Arguments.of("with null page token", new MCPMetadataResponse(List.of("foo_item"), null), Map.of("items", List.of("foo_item"))),
                Arguments.of("with page token", new MCPMetadataResponse(List.of("foo_item"), "foo_token"), Map.of("items", List.of("foo_item"), "next_page_token", "foo_token")));
    }
}
