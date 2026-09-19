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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.spi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SPITypeExistsValidatorTest {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validityArguments")
    void assertIsValid(final String name, final Object config, final boolean expected) {
        assertThat(VALIDATOR.validate(config).isEmpty(), is(expected));
    }
    
    private static Stream<Arguments> validityArguments() {
        return Stream.of(
                Arguments.of("Null", new DirectConfiguration(null), true),
                Arguments.of("Type", new DirectConfiguration("FIXTURE"), true),
                Arguments.of("Case insensitive type", new DirectConfiguration("fixture"), true),
                Arguments.of("Alias", new DirectConfiguration("ALIAS"), true),
                Arguments.of("Missing type", new DirectConfiguration("MISSING"), false),
                Arguments.of("Blank type", new DirectConfiguration(" "), false),
                Arguments.of("Nested type", new NestedConfiguration(new SPIConfiguration("FIXTURE")), true),
                Arguments.of("Missing nested type", new NestedConfiguration(new SPIConfiguration("MISSING")), false),
                Arguments.of("Empty map", new MapConfiguration(Collections.emptyMap()), true),
                Arguments.of("Map type", new MapConfiguration(Collections.singletonMap("foo", new SPIConfiguration("FIXTURE"))), true),
                Arguments.of("Missing map type", new MapConfiguration(Collections.singletonMap("foo", new SPIConfiguration("MISSING"))), false),
                Arguments.of("Null map value", new MapConfiguration(Collections.singletonMap("foo", null)), true),
                Arguments.of("Missing SPI class", new MissingSPIClassConfiguration("FIXTURE"), false));
    }
    
    @Test
    void assertNestedPropertyPath() {
        Collection<ConstraintViolation<NestedConfiguration>> actual = VALIDATOR.validate(new NestedConfiguration(new SPIConfiguration("MISSING")));
        assertThat(actual.iterator().next().getPropertyPath().toString(), is("provider"));
    }
    
    @Test
    void assertInvalidSPIClass() {
        assertThrows(ValidationException.class, () -> VALIDATOR.validate(new InvalidSPIClassConfiguration("FIXTURE")));
    }
    
    @Test
    void assertMissingTypeGetter() {
        assertThrows(ValidationException.class, () -> VALIDATOR.validate(new MissingTypeGetterConfiguration(new Object())));
    }
    
    @RequiredArgsConstructor
    private static final class DirectConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture")
        private final String type;
    }
    
    @RequiredArgsConstructor
    private static final class NestedConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture")
        private final SPIConfiguration provider;
    }
    
    @RequiredArgsConstructor
    private static final class MapConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture")
        private final Map<String, SPIConfiguration> providers;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class SPIConfiguration {
        
        private final String type;
    }
    
    @RequiredArgsConstructor
    private static final class MissingSPIClassConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.MissingSPITypeFixture")
        private final String type;
    }
    
    @RequiredArgsConstructor
    private static final class InvalidSPIClassConfiguration {
        
        @SPITypeExists(spiClassName = "java.lang.String")
        private final String type;
    }
    
    @RequiredArgsConstructor
    private static final class MissingTypeGetterConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture")
        private final Object provider;
    }
}
