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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPTransportJsonMapperFactoryTest {
    
    @Test
    void assertCreate() throws IOException {
        McpJsonMapper actual = MCPTransportJsonMapperFactory.create();
        assertThat(actual, isA(JacksonMcpJsonMapper.class));
        ObjectMapper actualObjectMapper = ((JacksonMcpJsonMapper) actual).getObjectMapper();
        assertTrue(actualObjectMapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"));
        assertFalse(actualObjectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertFalse(actualObjectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertThat(actualObjectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion(), is(Include.NON_NULL));
        assertThat(actual.writeValueAsString(new Payload("foo_bar", null)), is("{\"foo\":\"foo_bar\"}"));
        assertThat(actual.readValue("{\"foo\":\"foo_bar\",\"ignored\":\"bar_baz\"}", Payload.class), is(new Payload("foo_bar", null)));
        assertThat(actual.writeValueAsString(new DatePayload(LocalDate.of(2025, 4, 9))), is("{\"scheduledDate\":[2025,4,9]}"));
    }
    
    private record Payload(String foo, String bar) {
    }
    
    private record DatePayload(LocalDate scheduledDate) {
    }
}
