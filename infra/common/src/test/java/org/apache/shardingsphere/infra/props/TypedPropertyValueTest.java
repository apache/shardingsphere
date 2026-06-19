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

package org.apache.shardingsphere.infra.props;

import org.apache.shardingsphere.infra.props.exception.TypedPropertyValueException;
import org.apache.shardingsphere.infra.props.fixture.enums.TypedPropertyEnumFixture;
import org.apache.shardingsphere.infra.props.fixture.TypedPropertyKeyFixture;
import org.apache.shardingsphere.infra.props.fixture.typed.PropertiesTypedSPIFixture;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TypedPropertyValueTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getBooleanValueArguments")
    void assertGetBooleanValue(final String name, final TypedPropertyKey key, final String value, final boolean expectedValue) throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(key, value).getValue(), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNumberValueArguments")
    void assertGetNumberValue(final String name, final TypedPropertyKey key, final String value, final Object expectedValue) throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(key, value).getValue(), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidNumberValueArguments")
    void assertGetInvalidNumberValue(final String name, final TypedPropertyKey key) {
        assertThrows(TypedPropertyValueException.class, () -> new TypedPropertyValue(key, "test"));
    }
    
    @Test
    void assertGetStringValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.STRING_VALUE, "new_value").getValue(), is("new_value"));
    }
    
    @Test
    void assertGetEnumValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.ENUM_VALUE, TypedPropertyEnumFixture.FOO.name()).getValue(), is(TypedPropertyEnumFixture.FOO));
    }
    
    @Test
    void assertGetInvalidEnumValue() {
        assertThrows(TypedPropertyValueException.class, () -> new TypedPropertyValue(TypedPropertyKeyFixture.ENUM_VALUE, "BAR"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTypedSPIArguments")
    void assertGetTypedSPI(final String name, final String value, final Object expectedValue) throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.TYPED_SPI_VALUE, value).getValue(), is(expectedValue));
    }
    
    private static Stream<Arguments> getBooleanValueArguments() {
        return Stream.of(
                Arguments.of("primitive true", TypedPropertyKeyFixture.BOOLEAN_VALUE, Boolean.TRUE.toString(), true),
                Arguments.of("wrapper true", TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE, Boolean.TRUE.toString(), true),
                Arguments.of("invalid value returns false", TypedPropertyKeyFixture.BOOLEAN_VALUE, "test", false));
    }
    
    private static Stream<Arguments> getNumberValueArguments() {
        return Stream.of(
                Arguments.of("int primitive", TypedPropertyKeyFixture.INT_VALUE, "1000", 1000),
                Arguments.of("int wrapper", TypedPropertyKeyFixture.INT_OBJECT_VALUE, "1000", 1000),
                Arguments.of("long primitive", TypedPropertyKeyFixture.LONG_VALUE, "10000", 10000L),
                Arguments.of("long wrapper", TypedPropertyKeyFixture.LONG_OBJECT_VALUE, "10000", 10000L),
                Arguments.of("float primitive", createTypedPropertyKey(float.class), "3.14", 3.14F),
                Arguments.of("float wrapper", createTypedPropertyKey(Float.class), "3.14", 3.14F),
                Arguments.of("double primitive", createTypedPropertyKey(double.class), "6.28", 6.28D),
                Arguments.of("double wrapper", createTypedPropertyKey(Double.class), "6.28", 6.28D));
    }
    
    private static Stream<Arguments> getInvalidNumberValueArguments() {
        return Stream.of(
                Arguments.of("int primitive", TypedPropertyKeyFixture.INT_VALUE),
                Arguments.of("long primitive", TypedPropertyKeyFixture.LONG_VALUE),
                Arguments.of("float primitive", createTypedPropertyKey(float.class)),
                Arguments.of("double primitive", createTypedPropertyKey(double.class)));
    }
    
    private static Stream<Arguments> getTypedSPIArguments() {
        return Stream.of(
                Arguments.of("service value", "TYPED.SPI.PROPS", TypedSPILoader.getService(PropertiesTypedSPIFixture.class, "TYPED.SPI.PROPS")),
                Arguments.of("empty value", "", null),
                Arguments.of("null value", null, null));
    }
    
    private static TypedPropertyKey createTypedPropertyKey(final Class<?> type) {
        TypedPropertyKey result = mock(TypedPropertyKey.class);
        when(result.getKey()).thenReturn("key");
        doReturn(type).when(result).getType();
        return result;
    }
}
