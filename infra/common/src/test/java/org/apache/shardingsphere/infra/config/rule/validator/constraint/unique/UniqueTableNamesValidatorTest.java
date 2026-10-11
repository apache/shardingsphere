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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.unique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UniqueTableNamesValidatorTest {
    
    private final UniqueTableNamesValidator validator = new UniqueTableNamesValidator();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validTablesArguments")
    void assertIsValidWithoutDuplicatedNames(final String name, final Collection<?> tables) {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        assertTrue(validator.isValid(tables, context));
        verifyNoInteractions(context);
    }
    
    private static Stream<Arguments> validTablesArguments() {
        return Stream.of(
                Arguments.of("Empty tables", Collections.emptyList()),
                Arguments.of("Single table", createTables("foo_tbl")),
                Arguments.of("Distinct table names", createTables("foo_tbl", "bar_tbl")),
                Arguments.of("Table names remain case sensitive", createTables("foo_tbl", "FOO_TBL")));
    }
    
    private static Collection<Table> createTables(final String... tableNames) {
        return Arrays.stream(tableNames).map(Table::new).collect(Collectors.toList());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicatedTablesArguments")
    void assertIsValidWithDuplicatedNames(final String name, final Collection<?> tables, final String expectedMessage) {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(expectedMessage)).thenReturn(builder);
        assertFalse(validator.isValid(tables, context));
        verify(context).disableDefaultConstraintViolation();
        verify(builder).addConstraintViolation();
    }
    
    private static Stream<Arguments> duplicatedTablesArguments() {
        return Stream.of(
                Arguments.of("Nonadjacent duplicated name", createTables("foo_tbl", "bar_tbl", "foo_tbl"), "must not contain duplicate table names `foo_tbl`"),
                Arguments.of("All duplicated names retain encounter order", createTables("z_tbl", "a_tbl", "z_tbl", "a_tbl", "z_tbl"),
                        "must not contain duplicate table names `z_tbl, a_tbl`"),
                Arguments.of("Names read across different table types", Arrays.asList(new Table("foo_tbl"), new OtherTable("foo_tbl")), "must not contain duplicate table names `foo_tbl`"));
    }
    
    @Test
    void assertIsValidWithMissingNameProperty() {
        ValidationException actual = assertThrows(ValidationException.class, () -> validator.isValid(Collections.singleton(new Object()), mock(ConstraintValidatorContext.class)));
        assertThat(actual.getMessage(), is("Can not read property `name` from `java.lang.Object`."));
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class Table {
        
        private final String name;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class OtherTable {
        
        private final String name;
    }
}
