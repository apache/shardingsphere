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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPModelFacingToolResponseFormatterTest {
    
    @Test
    void assertFormat() {
        Map<String, Object> actual = JsonUtils.fromJsonString(LLMMCPModelFacingToolResponseFormatter.format(Map.of("resources", List.of(Map.of(
                "uri", "shardingsphere://databases",
                "name", "logical-databases",
                "title", "Logical Databases",
                "description", "Long model-facing description.",
                "mimeType", "application/json",
                "_meta", Map.of("org.apache.shardingsphere/resource-kind", "list"))))), new TypeReference<>() {
                });
        Map<String, Object> expected = Map.of("resources", List.of(Map.of(
                "uri", "shardingsphere://databases",
                "name", "logical-databases",
                "title", "Logical Databases",
                "mimeType", "application/json")));
        assertThat(actual, is(expected));
    }
}
