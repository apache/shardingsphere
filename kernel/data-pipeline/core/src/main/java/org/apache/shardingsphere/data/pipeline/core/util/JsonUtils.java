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

package org.apache.shardingsphere.data.pipeline.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;

/**
 * Json utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JsonUtils {
    
    private static final ObjectMapper OBJECT_MAPPER;
    
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * To json string.
     *
     * @param value value
     * @return json string
     * @throws PipelineInternalException pipeline internal exception
     */
    public static String toJson(final Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (final JsonProcessingException ex) {
            log.error("Convert to json failed, value={}", value, ex);
            throw new PipelineInternalException(ex);
        }
    }
    
    /**
     * Read json string.
     *
     * @param jsonString json string
     * @param clazz class
     * @param <T> type of class
     * @return object from json
     * @throws PipelineInternalException pipeline internal exception
     */
    public static <T> T readJson(final String jsonString, final Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (final JsonProcessingException ex) {
            log.error("Parse json failed, jsonString={}", jsonString, ex);
            throw new PipelineInternalException(ex);
        }
    }
}
