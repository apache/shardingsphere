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

package org.apache.shardingsphere.infra.datasource.pool.props.domain.custom;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CustomDataSourcePoolPropertiesTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getPropertiesArguments")
    void assertGetProperties(final String name, final Map<String, Object> props, final Collection<String> standardPropertyKeys,
                             final Collection<String> transientFieldNames, final Map<String, String> propertySynonyms, final Map<String, Object> expected) {
        assertThat(new CustomDataSourcePoolProperties(props, standardPropertyKeys, transientFieldNames, propertySynonyms).getProperties(), is(expected));
    }
    
    private static Stream<Arguments> getPropertiesArguments() {
        return Stream.of(
                Arguments.of("filtered simple properties", createSimpleProperties(), Arrays.asList("username", "password", "closed"),
                        Collections.singletonList("closed"), Collections.singletonMap("username", "user"), Collections.singletonMap("foo", "bar")),
                Arguments.of("new complex property group", createSingleComplexProperty(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.singletonMap("fooProperties", PropertiesBuilder.build(new Property("foo1", "foo_value_1")))),
                Arguments.of("existing complex property group", createRepeatedComplexProperties(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.singletonMap("fooProperties", PropertiesBuilder.build(new Property("foo1", "foo_value_1"), new Property("foo2", "foo_value_2")))),
                Arguments.of("nested complex key", createNestedComplexProperty(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.singletonMap("fooProperties.foo1.bar", "foo_value_1")));
    }
    
    private static Map<String, Object> createSimpleProperties() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("user", "root");
        result.put("password", "root");
        result.put("closed", false);
        result.put("foo", "bar");
        return result;
    }
    
    private static Map<String, Object> createSingleComplexProperty() {
        return Collections.singletonMap("fooProperties.foo1", "foo_value_1");
    }
    
    private static Map<String, Object> createRepeatedComplexProperties() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("fooProperties.foo1", "foo_value_1");
        result.put("fooProperties.foo2", "foo_value_2");
        return result;
    }
    
    private static Map<String, Object> createNestedComplexProperty() {
        return Collections.singletonMap("fooProperties.foo1.bar", "foo_value_1");
    }
}
