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

package org.apache.shardingsphere.test.e2e.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LLMStructuredAnswer {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final String database;
    
    private final String schema;
    
    private final String table;
    
    private final String query;
    
    private final int totalOrders;
    
    private final List<String> toolSequence;
    
    LLMStructuredAnswer(final String database, final String schema, final String table, final String query,
                        final int totalOrders, final List<String> toolSequence) {
        this.database = database;
        this.schema = schema;
        this.table = table;
        this.query = query;
        this.totalOrders = totalOrders;
        this.toolSequence = List.copyOf(toolSequence);
    }
    
    static LLMStructuredAnswer fromJson(final String json) {
        final Map<String, Object> payload = readPayload(json);
        final List<String> toolSequence = new LinkedList<>();
        final Object rawToolSequence = payload.get("toolSequence");
        if (rawToolSequence instanceof Iterable) {
            for (Object each : (Iterable<?>) rawToolSequence) {
                toolSequence.add(Objects.toString(each, ""));
            }
        }
        return new LLMStructuredAnswer(
                Objects.toString(payload.get("database"), "").trim(),
                Objects.toString(payload.get("schema"), "").trim(),
                Objects.toString(payload.get("table"), "").trim(),
                Objects.toString(payload.get("query"), "").trim(),
                parseTotalOrders(payload.get("totalOrders")),
                toolSequence);
    }
    
    String database() {
        return database;
    }
    
    String schema() {
        return schema;
    }
    
    String table() {
        return table;
    }
    
    String query() {
        return query;
    }
    
    int totalOrders() {
        return totalOrders;
    }
    
    List<String> toolSequence() {
        return toolSequence;
    }
    
    String getNormalizedQuery() {
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
}
