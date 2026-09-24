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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonEngine;
import org.apache.shardingsphere.infra.util.json.JsonException;
import org.apache.shardingsphere.infra.util.json.JsonTypeReference;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LLMMCPJsonValues {
    
    static Map<String, Object> parseToolArguments(final String argumentsJson) {
        try {
            return JsonEngine.unmarshal(argumentsJson, new JsonTypeReference<Map<String, Object>>() {
            });
        } catch (final JsonException ex) {
            throw new IllegalArgumentException("Invalid tool arguments JSON.", ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    static Map<String, Object> castToMap(final Object value) {
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Expected a JSON object.");
        }
        Map<?, ?> result = (Map<?, ?>) value;
        if (!result.keySet().stream().allMatch(String.class::isInstance)) {
            throw new IllegalArgumentException("Expected a JSON object with string keys.");
        }
        return (Map<String, Object>) result;
    }
    
}
