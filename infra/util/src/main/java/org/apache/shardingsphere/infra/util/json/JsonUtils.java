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
import lombok.SneakyThrows;

/**
 * Json utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {
    
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
     * Parse data to json string.
     *
     * @param data data
     * @return json string
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String toJsonString(final Object data) {
        return MAPPER.writeValueAsString(data);
    }
    
    /**
     * Deserialize to Object from json string.
     *
     * @param value json string
     * @param clazz target Object
     * @param <T> the type of return Object data
     * @return target Object data
     */
    @SneakyThrows(JsonProcessingException.class)
    public static <T> T readValue(final String value, final Class<T> clazz) {
        return MAPPER.readValue(value, clazz);
    }
}
