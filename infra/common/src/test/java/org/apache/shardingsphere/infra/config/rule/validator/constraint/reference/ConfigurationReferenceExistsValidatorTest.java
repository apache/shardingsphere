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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationReferenceExistsValidatorTest {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    @Test
    void assertConfiguredDirectReference() {
        assertTrue(VALIDATOR.validate(new DirectConfiguration("foo_provider", Collections.singletonMap("foo_provider", "configured"))).isEmpty());
    }
    
    @Test
    void assertUnconfiguredDirectReference() {
        Set<ConstraintViolation<DirectConfiguration>> actual = VALIDATOR.validate(new DirectConfiguration("bar_provider", Collections.singletonMap("foo_provider", "configured")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getPropertyPath().toString(), is("providerName"));
        assertThat(actual.iterator().next().getMessage(), is("references unconfigured providers `bar_provider`"));
    }
    
    @Test
    void assertUnconfiguredReferenceWithMissingPool() {
        Set<ConstraintViolation<DirectConfiguration>> actual = VALIDATOR.validate(new DirectConfiguration("bar_provider", null));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getMessage(), is("references unconfigured providers `bar_provider`"));
    }
    
    @Test
    void assertConfiguredNestedReference() {
        NestedConfiguration config = new NestedConfiguration(Collections.singleton(new Group(Collections.singleton(
                new Item(new Reference("foo_algorithm"), null)))), Collections.singletonMap("foo_algorithm", "configured"));
        assertTrue(VALIDATOR.validate(config).isEmpty());
    }
    
    @Test
    void assertFirstUnconfiguredNestedReference() {
        NestedConfiguration config = new NestedConfiguration(Collections.singleton(new Group(Collections.singleton(
                new Item(new Reference("bar_algorithm"), new Reference("baz_algorithm"))))), Collections.emptyMap());
        Set<ConstraintViolation<NestedConfiguration>> actual = VALIDATOR.validate(config);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getPropertyPath().toString(), is("groups"));
        assertThat(actual.iterator().next().getMessage(), is("references unconfigured algorithms `bar_algorithm`"));
    }
    
    @Test
    void assertReferencesValidatedPerSourceObject() {
        Item first = new Item(new Reference("foo_algorithm"), new Reference("first_missing"));
        Item second = new Item(new Reference("second_missing"), null);
        NestedConfiguration config = new NestedConfiguration(Collections.singleton(new Group(Arrays.asList(first, second))),
                Collections.singletonMap("foo_algorithm", "configured"));
        Set<ConstraintViolation<NestedConfiguration>> actual = VALIDATOR.validate(config);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getMessage(), is("references unconfigured algorithms `first_missing`"));
    }
    
    @Test
    void assertEmptyReference() {
        NestedConfiguration config = new NestedConfiguration(Collections.singleton(new Group(Collections.singleton(new Item(new Reference(""), null)))), Collections.emptyMap());
        assertTrue(VALIDATOR.validate(config).isEmpty());
    }
    
    @Test
    void assertEmptyReferenceWithMissingPool() {
        NestedConfiguration config = new NestedConfiguration(Collections.singleton(new Group(Collections.singleton(new Item(new Reference(""), null)))), null);
        assertTrue(VALIDATOR.validate(config).isEmpty());
    }
    
    @Test
    void assertOptionalReference() {
        NestedConfiguration config = new NestedConfiguration(Collections.singleton(new Group(Collections.singleton(
                new Item(new Reference("foo_algorithm"), new Reference("foo_algorithm"))))), Collections.singletonMap("foo_algorithm", "configured"));
        assertTrue(VALIDATOR.validate(config).isEmpty());
    }
    
    @Test
    void assertDisallowedEmptyReference() {
        Set<ConstraintViolation<DirectConfiguration>> actual = VALIDATOR.validate(new DirectConfiguration("", Collections.emptyMap()));
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertInvalidReferenceProperty() {
        assertThrows(ValidationException.class, () -> VALIDATOR.validate(new InvalidReferenceConfiguration(Collections.emptyMap())));
    }
    
    @Test
    void assertInvalidPoolProperty() {
        assertThrows(ValidationException.class, () -> VALIDATOR.validate(new InvalidPoolConfiguration("not a map")));
    }
    
    @RequiredArgsConstructor
    @Getter
    @ConfigurationReferenceExists(referencePaths = "providerName", pool = "providers")
    public static final class DirectConfiguration {
        
        private final String providerName;
        
        private final Map<String, String> providers;
    }
    
    @RequiredArgsConstructor
    @Getter
    @ConfigurationReferenceExists(referencePaths = {"groups.items.primary.name", "groups.items.secondary.name"}, pool = "algorithms", allowEmpty = true)
    public static final class NestedConfiguration {
        
        private final Collection<Group> groups;
        
        private final Map<String, String> algorithms;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class Group {
        
        private final Collection<Item> items;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class Item {
        
        private final Reference primary;
        
        private final Reference secondary;
        
        public Optional<Reference> getSecondary() {
            return Optional.ofNullable(secondary);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class Reference {
        
        private final String name;
    }
    
    @RequiredArgsConstructor
    @Getter
    @ConfigurationReferenceExists(referencePaths = "missing", pool = "providers")
    public static final class InvalidReferenceConfiguration {
        
        private final Map<String, String> providers;
    }
    
    @RequiredArgsConstructor
    @Getter
    @ConfigurationReferenceExists(referencePaths = "pool", pool = "pool")
    public static final class InvalidPoolConfiguration {
        
        private final String pool;
    }
}
