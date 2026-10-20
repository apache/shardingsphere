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

package org.apache.shardingsphere.mcp.support.resource;

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPUriTemplateTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTemplates")
    void assertCreateWithInvalidPath(final String name, final String template, final String expectedMessage) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new MCPUriTemplate(template));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Collection<Arguments> invalidTemplates() {
        return List.of(
                Arguments.of("unsupported scheme", "unsupported://capabilities", "Invalid URI `unsupported://capabilities`."),
                Arguments.of("missing scheme separator", "invalid-template", "Invalid URI `invalid-template`."),
                Arguments.of("embedded variable", "shardingsphere://databases/prefix-{database}",
                        "URI template variables must occupy a complete path segment in `shardingsphere://databases/prefix-{database}`."),
                Arguments.of("empty path", "shardingsphere://", "URI path is required for `shardingsphere://`."),
                Arguments.of("empty segment", "shardingsphere://databases//schemas", "URI segment is required for `shardingsphere://databases//schemas`."),
                Arguments.of("nested variable marker", "shardingsphere://databases/{{database}}", "Invalid URI template variable `{database}` in `shardingsphere://databases/{{database}}`."),
                Arguments.of("duplicate variable", "shardingsphere://databases/{database}/{database}",
                        "Duplicate URI template variable `database` in `shardingsphere://databases/{database}/{database}`."));
    }
    
    @Test
    void assertGetVariableNames() {
        List<String> actualVariableNames = new MCPUriTemplate("shardingsphere://databases/{database}/schemas/{schema}").getVariableNames();
        assertThat(actualVariableNames, is(List.of("database", "schema")));
    }
    
    @Test
    void assertExpandIfComplete() {
        Optional<String> actualUri = new MCPUriTemplate("shardingsphere://databases/{database}/schemas/{schema}")
                .expandIfComplete(new MCPResourceURIVariables(Map.of("database", "logic_db", "schema", "public")));
        assertThat(actualUri, is(Optional.of("shardingsphere://databases/logic_db/schemas/public")));
    }
    
    @Test
    void assertExpandIfCompleteWithMissingVariable() {
        Optional<String> actualUri = new MCPUriTemplate("shardingsphere://databases/{database}/schemas/{schema}")
                .expandIfComplete(new MCPResourceURIVariables(Map.of("database", "logic_db")));
        assertThat(actualUri, is(Optional.empty()));
    }
    
    @Test
    void assertParse() {
        Optional<MCPResourceURIVariables> actual = new MCPUriTemplate("shardingsphere://databases/{database}/tables/{table}")
                .parse("shardingsphere://databases/logic_db/tables/orders%20archive%2F2026+raw%3F");
        assertThat(actual.orElseThrow().getValue("table"), is("orders archive/2026+raw?"));
    }
    
    @Test
    void assertParseWithMalformedPercentEncoding() {
        assertFalse(new MCPUriTemplate("shardingsphere://databases/{database}").parse("shardingsphere://databases/logic%ZZdb").isPresent());
    }
    
    @Test
    void assertParseWithUnexpandedVariable() {
        assertFalse(new MCPUriTemplate("shardingsphere://databases/{database}").parse("shardingsphere://databases/{database}").isPresent());
    }
    
    @Test
    void assertParseWithUnmatchedURI() {
        assertFalse(new MCPUriTemplate("shardingsphere://capabilities").parse("unsupported://capabilities").isPresent());
    }
    
    @Test
    void assertOverlaps() {
        assertTrue(new MCPUriTemplate("shardingsphere://databases/{database}").overlaps(new MCPUriTemplate("shardingsphere://databases/logic_db")));
    }
    
}
