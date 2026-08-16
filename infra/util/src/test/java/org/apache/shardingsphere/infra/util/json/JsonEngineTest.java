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

import lombok.Getter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonEngineTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getJsonConfigurationArguments")
    void assertJsonConfigurations(final String name, final Object value, final String expected) {
        assertThat(JsonEngine.toJsonString(value), is(expected));
    }
    
    private static Stream<Arguments> getJsonConfigurationArguments() {
        return Stream.of(
                Arguments.of("empty beans", new Object(), "{}"),
                Arguments.of("Java time", Collections.singletonMap("date", LocalDate.of(2025, 4, 9)), "{\"date\":[2025,4,9]}"),
                Arguments.of("JDK optional", Collections.singletonMap("optional", Optional.of("foo")), "{\"optional\":\"foo\"}"),
                Arguments.of("null property", Collections.singletonMap("ignored", null), "{}"));
    }
    
    @Test
    void assertToJsonString() {
        assertThat(JsonEngine.toJsonString(Collections.singletonMap("k", "v")), is("{\"k\":\"v\"}"));
    }
    
    @Test
    void assertToPrettyJsonString() {
        assertThat(JsonEngine.toPrettyJsonString(Collections.singletonMap("k", "v")), is("{" + System.lineSeparator() + "  \"k\" : \"v\"" + System.lineSeparator() + "}"));
    }
    
    @Test
    void assertFromJsonStringToClass() {
        assertThat(JsonEngine.fromJsonString("{\"name\":\"foo\",\"ignored\":true}", JsonConfigurationFixture.class).getName(), is("foo"));
    }
    
    @Test
    void assertFromJsonStringToJsonTypeReference() {
        List<JsonConfigurationFixture> actual = JsonEngine.fromJsonString("[{\"name\":\"foo\"}]", new JsonTypeReference<List<JsonConfigurationFixture>>() {
        });
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo"));
    }
    
    @Test
    void assertFromMalformedJsonString() {
        assertThrows(JsonException.class, () -> JsonEngine.fromJsonString("{", Object.class));
    }
    
    @Test
    void assertSerializeCyclicObject() {
        assertThrows(JsonException.class, () -> JsonEngine.toJsonString(new CyclicFixture()));
    }
    
    @Getter
    private static final class CyclicFixture {
        
        private final CyclicFixture value = this;
    }
}
