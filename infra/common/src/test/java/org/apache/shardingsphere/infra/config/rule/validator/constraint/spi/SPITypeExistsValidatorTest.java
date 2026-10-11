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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.RuleConfigurationValidationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SPITypeExistsValidatorTest {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    private final SPITypeExistsValidator validator = new SPITypeExistsValidator();
    
    private final SPITypeExists constraintAnnotation = mock(SPITypeExists.class);
    
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
                Arguments.of("Null map", new MapConfiguration(null), true),
                Arguments.of("Default nested type", new NestedConfiguration(new SPIConfiguration(null)), true),
                Arguments.of("Repeated map type", new MapConfiguration(createProviders(new SPIConfiguration("FIXTURE"), new SPIConfiguration("FIXTURE"))), true),
                Arguments.of("String map types", new MapConfiguration(createProviders("FIXTURE", "ALIAS")), true),
                Arguments.of("Different map classes", new MapConfiguration(createProviders(new SPIConfiguration("FIXTURE"), new ObjectSPIConfiguration("ALIAS"))), true),
                Arguments.of("Mixed map types", new MapConfiguration(createProviders("FIXTURE", "fixture", "ALIAS", new SPIConfiguration(null))), true),
                Arguments.of("Case sensitive alias", new MapConfiguration(createProviders("FIXTURE", "alias")), false),
                Arguments.of("Non-string map type", new MapConfiguration(createProviders("FIXTURE", new ObjectSPIConfiguration(new UnhashableType()))), false),
                Arguments.of("Default map types", new MapConfiguration(createProviders(new SPIConfiguration(null), new ObjectSPIConfiguration(null))), true),
                Arguments.of("Null before map type", new MapConfiguration(createProviders(null, new SPIConfiguration("FIXTURE"))), true));
    }
    
    private static Map<String, Object> createProviders(final Object... values) {
        Map<String, Object> result = new LinkedHashMap<>(values.length, 1F);
        for (int index = 0; index < values.length; index++) {
            result.put("foo_provider_" + index, values[index]);
        }
        return result;
    }
    
    @Test
    void assertRepeatedTypeQueries() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            mockedLoader.when(() -> TypedSPILoader.containsService(eq(SPITypeFixture.class), any())).thenReturn(true);
            assertTrue(validator.isValid(createProviders("FIXTURE", new SPIConfiguration("FIXTURE"), "fixture", "ALIAS", "ALIAS",
                    new SPIConfiguration(null), new ObjectSPIConfiguration(null)), null));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, "FIXTURE"));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, "fixture"));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, "ALIAS"));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, null));
            mockedLoader.verifyNoMoreInteractions();
        }
    }
    
    @Test
    void assertNonStringTypeQueries() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            mockedLoader.when(() -> TypedSPILoader.containsService(SPITypeFixture.class, 42)).thenReturn(true);
            assertTrue(validator.isValid(createProviders(new ObjectSPIConfiguration(42), new ObjectSPIConfiguration(42)), null));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, 42), times(2));
        }
    }
    
    @Test
    void assertMissingDefaultType() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            assertFalse(validator.isValid(createProviders(new SPIConfiguration(null), "FIXTURE"), null));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, null));
            mockedLoader.verifyNoMoreInteractions();
        }
    }
    
    @Test
    void assertEveryTypeGetter() {
        SPIConfiguration provider = mock(SPIConfiguration.class);
        when(provider.getType()).thenReturn("FIXTURE");
        assertTrue(VALIDATOR.validate(new MapConfiguration(createProviders(provider, null, provider))).isEmpty());
        verify(provider, times(2)).getType();
    }
    
    @Test
    void assertCurrentTypeForRepeatedObject() {
        SPIConfiguration provider = mock(SPIConfiguration.class);
        when(provider.getType()).thenReturn("FIXTURE", "MISSING");
        assertFalse(VALIDATOR.validate(new MapConfiguration(createProviders(provider, provider))).isEmpty());
        verify(provider, times(2)).getType();
    }
    
    @Test
    void assertTypeChangeBetweenCalls() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        SPIConfiguration provider = mock(SPIConfiguration.class);
        when(provider.getType()).thenReturn("FIXTURE");
        Map<String, Object> providers = createProviders(provider, provider);
        assertTrue(validator.isValid(providers, null));
        when(provider.getType()).thenReturn("MISSING");
        assertFalse(validator.isValid(providers, null));
        verify(provider, times(3)).getType();
    }
    
    @Test
    void assertLookupResultBetweenCalls() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            mockedLoader.when(() -> TypedSPILoader.containsService(SPITypeFixture.class, "FIXTURE")).thenReturn(true, false);
            Map<String, Object> providers = createProviders("FIXTURE", "FIXTURE");
            assertTrue(validator.isValid(providers, null));
            assertFalse(validator.isValid(providers, null));
            mockedLoader.verify(() -> TypedSPILoader.containsService(SPITypeFixture.class, "FIXTURE"), times(2));
        }
    }
    
    @Test
    void assertClassLoaderBetweenCalls() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        Map<String, Object> providers = createProviders("FIXTURE", "FIXTURE");
        assertTrue(validator.isValid(providers, null));
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            ValidationException actual = assertThrows(ValidationException.class, () -> validator.isValid(providers, null));
            assertThat(actual.getCause(), isA(ClassNotFoundException.class));
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
    }
    
    @Test
    void assertShortCircuit() {
        SPIConfiguration provider = mock(SPIConfiguration.class);
        assertFalse(VALIDATOR.validate(new MapConfiguration(createProviders("MISSING", provider))).isEmpty());
        verifyNoInteractions(provider);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("lazySPIClassArguments")
    void assertLazySPIClass(final String name, final Object config) {
        assertTrue(VALIDATOR.validate(config).isEmpty());
    }
    
    private static Stream<Arguments> lazySPIClassArguments() {
        return Stream.of(
                Arguments.of("Missing class empty map", new MissingSPIClassConfiguration(Collections.emptyMap())),
                Arguments.of("Missing class null map", new MissingSPIClassConfiguration(null)),
                Arguments.of("Missing class null values", new MissingSPIClassConfiguration(createProviders(null, null))),
                Arguments.of("Invalid class empty map", new InvalidSPIClassConfiguration(Collections.emptyMap())),
                Arguments.of("Invalid class null values", new InvalidSPIClassConfiguration(createProviders(null, null))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("typeGetterFailureArguments")
    void assertTypeGetterFailure(final String name, final Object provider, final Class<? extends ReflectiveOperationException> expectedCause) {
        when(constraintAnnotation.spiClassName()).thenReturn("missing");
        validator.initialize(constraintAnnotation);
        ValidationException actual = assertThrows(ValidationException.class, () -> validator.isValid(createProviders(provider, "FIXTURE"), null));
        assertThat(actual.getMessage(), is(String.format("Can not read property `type` from `%s`.", provider.getClass().getName())));
        assertThat(actual.getCause(), isA(expectedCause));
    }
    
    private static Stream<Arguments> typeGetterFailureArguments() {
        return Stream.of(
                Arguments.of("Missing getter", new Object(), NoSuchMethodException.class),
                Arguments.of("Private getter", new PrivateTypeGetterConfiguration(), NoSuchMethodException.class),
                Arguments.of("Throwing getter", new ThrowingSPIConfiguration(), InvocationTargetException.class));
    }
    
    @Test
    void assertRepeatedGetterFailure() {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        SPIConfiguration provider = mock(SPIConfiguration.class);
        IllegalStateException expected = new IllegalStateException("foo_getter");
        when(provider.getType()).thenReturn("FIXTURE").thenThrow(expected);
        ValidationException actual = assertThrows(ValidationException.class, () -> validator.isValid(createProviders(provider, provider), null));
        assertThat(actual.getCause(), isA(InvocationTargetException.class));
        assertThat(actual.getCause().getCause(), sameInstance(expected));
    }
    
    @Test
    void assertMissingSPIClassInMap() {
        when(constraintAnnotation.spiClassName()).thenReturn("missing");
        validator.initialize(constraintAnnotation);
        ValidationException actual = assertThrows(ValidationException.class, () -> validator.isValid(createProviders(new SPIConfiguration("FIXTURE"), "FIXTURE"), null));
        assertThat(actual.getMessage(), is("Can not load SPI class `missing`."));
        assertThat(actual.getCause(), isA(ClassNotFoundException.class));
    }
    
    @Test
    void assertInvalidSPIClassInMap() {
        when(constraintAnnotation.spiClassName()).thenReturn(String.class.getName());
        validator.initialize(constraintAnnotation);
        ValidationException actual = assertThrows(ValidationException.class, () -> validator.isValid(createProviders(new SPIConfiguration("FIXTURE"), "FIXTURE"), null));
        assertThat(actual.getMessage(), is("Class `java.lang.String` does not implement TypedSPI."));
        assertNull(actual.getCause());
    }
    
    @Test
    void assertConcurrentCalls() throws Exception {
        when(constraintAnnotation.spiClassName()).thenReturn(SPITypeFixture.class.getName());
        validator.initialize(constraintAnnotation);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Future<Boolean> valid = executor.submit(() -> {
                barrier.await(5, TimeUnit.SECONDS);
                return validator.isValid(createProviders(new SPIConfiguration("FIXTURE"), new SPIConfiguration(null)), null);
            });
            Future<Boolean> invalid = executor.submit(() -> {
                barrier.await(5, TimeUnit.SECONDS);
                return validator.isValid(createProviders(new ObjectSPIConfiguration("MISSING"), "FIXTURE"), null);
            });
            assertTrue(valid.get(5, TimeUnit.SECONDS));
            assertFalse(invalid.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    void assertMapPropertyPath() {
        Collection<ConstraintViolation<MapConfiguration>> actual = VALIDATOR.validate(new MapConfiguration(createProviders("FIXTURE", "MISSING")));
        assertThat(actual.iterator().next().getPropertyPath().toString(), is("providers"));
    }
    
    @Test
    void assertPublicValidationException() {
        MapConfiguration owner = new MapConfiguration(Collections.emptyMap());
        RuleConfigurationValidationException actual = assertThrows(RuleConfigurationValidationException.class,
                () -> RuleConfigurationValidator.validateRuleItem(owner, new MissingSPIClassConfiguration(createProviders("FIXTURE", "FIXTURE"))));
        assertThat(actual.getCause(), isA(ValidationException.class));
    }
    
    @Test
    void assertPublicValidationViolation() {
        InvalidRuleConfigurationException actual = assertThrows(InvalidRuleConfigurationException.class,
                () -> RuleConfigurationValidator.validate(new MapConfiguration(createProviders("FIXTURE", "MISSING"))));
        assertThat(actual.getMessage(), is("Invalid 'MapConfiguration' rule, error message is: Property `providers` does not match an available SPI implementation."));
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
    void assertMissingSPIClass() {
        assertThrows(ValidationException.class, () -> VALIDATOR.validate(new MissingSPIClassConfiguration("FIXTURE")));
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
    private static final class MapConfiguration implements RuleConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture")
        private final Map<String, ?> providers;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class SPIConfiguration {
        
        private final String type;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class ObjectSPIConfiguration {
        
        private final Object type;
    }
    
    @Getter(AccessLevel.PRIVATE)
    private static final class PrivateTypeGetterConfiguration {
        
        private final String type = "FIXTURE";
    }
    
    public static final class ThrowingSPIConfiguration {
        
        public String getType() {
            throw new IllegalStateException("foo_getter");
        }
    }
    
    private static final class UnhashableType {
        
        @Override
        public boolean equals(final Object value) {
            return this == value;
        }
        
        @Override
        public int hashCode() {
            throw new IllegalStateException("foo_hash");
        }
    }
    
    @RequiredArgsConstructor
    private static final class MissingSPIClassConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.MissingSPITypeFixture")
        private final Object type;
    }
    
    @RequiredArgsConstructor
    private static final class InvalidSPIClassConfiguration {
        
        @SPITypeExists(spiClassName = "java.lang.String")
        private final Object type;
    }
    
    @RequiredArgsConstructor
    private static final class MissingTypeGetterConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.SPITypeFixture")
        private final Object provider;
    }
}
