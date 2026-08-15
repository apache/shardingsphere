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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilsTest {
    
    @Test
    void assertCreateObjectMapper() {
        ObjectMapper actual = JsonUtils.createObjectMapper();
        assertTrue(actual.getRegisteredModuleIds().contains("jackson-datatype-jsr310"));
        assertTrue(actual.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype.jdk8.Jdk8Module"));
        assertFalse(actual.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertFalse(actual.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertThat(actual.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion(), is(Include.NON_NULL));
        actual.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        assertThat(JsonUtils.toJsonString(new Object()), is("{}"));
    }
    
    @Test
    void assertToJsonString() {
        assertThat(JsonUtils.toJsonString(Collections.singletonMap("k", "v")), is("{\"k\":\"v\"}"));
    }
    
    @Test
    void assertFromJsonStringToClass() {
        assertThat(JsonUtils.fromJsonString("{\"name\":\"foo\"}", JsonConfigurationFixture.class).getName(), is("foo"));
    }
    
    @Test
    void assertFromJsonStringToTypeReference() {
        List<JsonConfigurationFixture> actual = JsonUtils.fromJsonString("[{\"name\":\"foo\"}]", new TypeReference<List<JsonConfigurationFixture>>() {
        });
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo"));
    }
}
