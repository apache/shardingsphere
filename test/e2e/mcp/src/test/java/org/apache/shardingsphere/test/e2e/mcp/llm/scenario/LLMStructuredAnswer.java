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

package org.apache.shardingsphere.test.e2e.mcp.llm.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
public final class LLMStructuredAnswer {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final String database;
    
    private final String schema;
    
    private final String table;
    
    private final String query;
    
    private final int totalOrders;
    
    private final List<String> interactionSequence;
    
    /**
     * Grom json.
     *
     * @param json json
     * @return LLM structured answer
     */
    public static LLMStructuredAnswer fromJson(final String json) {
        final Map<String, Object> payload = readPayload(json);
        List<String> interactionSequence = createInteractionSequence(payload);
        return new LLMStructuredAnswer(
                Objects.toString(payload.get("database"), "").trim(),
                Objects.toString(payload.get("schema"), "").trim(),
                Objects.toString(payload.get("table"), "").trim(),
                Objects.toString(payload.get("query"), "").trim(),
                parseTotalOrders(payload.get("totalOrders")),
                interactionSequence);
    }
    
    /**
     * Get normalized query.
     *
     * @return normalized query
     */
    public String getNormalizedQuery() {
        return query.replaceAll("\\s+", " ").trim();
    }
    
    private static Map<String, Object> readPayload(final String json) {
        try {
            final Map<String, Object> result = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
            if (null == result) {
                throw new IllegalArgumentException("Structured answer JSON must decode to one object.");
            }
            return result;
        } catch (final JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid structured answer JSON.", ex);
        }
    }
    
    private static int parseTotalOrders(final Object totalOrders) {
        if (totalOrders instanceof Number) {
            return ((Number) totalOrders).intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(totalOrders, "").trim());
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid totalOrders value.", ex);
        }
    }
    
    private static List<String> createInteractionSequence(final Map<String, Object> payload) {
        List<String> result = new LinkedList<>();
        Object rawInteractionSequence = payload.containsKey("interactionSequence") ? payload.get("interactionSequence") : payload.get("toolSequence");
        if (rawInteractionSequence instanceof Iterable) {
            for (Object each : (Iterable<?>) rawInteractionSequence) {
                result.add(Objects.toString(each, ""));
            }
        }
        return result;
    }
}
