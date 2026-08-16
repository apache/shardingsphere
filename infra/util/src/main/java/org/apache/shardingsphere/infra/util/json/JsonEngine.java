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

package org.apache.shardingsphere.infra.util.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JSON engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonEngine {
    
    private static final ObjectMapper MAPPER;
    
    static {
        MAPPER = initDefaultMapper();
    }
    
    private static ObjectMapper initDefaultMapper() {
        ObjectMapper result = new ObjectMapper();
        result.registerModule(new JavaTimeModule());
        result.findAndRegisterModules();
        result.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        result.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        result.setSerializationInclusion(Include.NON_NULL);
        return result;
    }
    
    /**
     * Serialize object as json string.
     *
     * @param obj object
     * @return json string
     * @throws JsonException when object cannot be serialized
     */
    public static String toJsonString(final Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (final JsonProcessingException ex) {
            throw new JsonException(ex);
        }
    }
    
    /**
     * Serialize object as pretty JSON string.
     *
     * @param obj object
     * @return pretty JSON string
     * @throws JsonException when object cannot be serialized
     */
    public static String toPrettyJsonString(final Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (final JsonProcessingException ex) {
            throw new JsonException(ex);
        }
    }
    
    /**
     * Deserialize from json string to object.
     *
     * @param value json string
     * @param clazz target object type
     * @param <T> the type of target object
     * @return object
     * @throws JsonException when JSON cannot be deserialized
     */
    public static <T> T fromJsonString(final String value, final Class<T> clazz) {
        try {
            return MAPPER.readValue(value, clazz);
        } catch (final JsonProcessingException ex) {
            throw new JsonException(ex);
        }
    }
    
    /**
     * Deserialize from JSON string to object.
     *
     * @param value JSON string
     * @param typeReference target object type reference
     * @param <T> the type of target object
     * @return object
     * @throws JsonException when JSON cannot be deserialized
     */
    public static <T> T fromJsonString(final String value, final JsonTypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(value, MAPPER.getTypeFactory().constructType(typeReference.getType()));
        } catch (final JsonProcessingException ex) {
            throw new JsonException(ex);
        }
    }
}
