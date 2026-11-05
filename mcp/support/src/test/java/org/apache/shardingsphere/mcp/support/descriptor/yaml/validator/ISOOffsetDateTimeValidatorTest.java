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

package org.apache.shardingsphere.mcp.support.descriptor.yaml.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ISOOffsetDateTimeValidatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsValidCases")
    void assertIsValid(final String name, final String value, final boolean expected) {
        boolean actual = new ISOOffsetDateTimeValidator().isValid(value, mock(ConstraintValidatorContext.class));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertIsValidWithBlankValue() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate("is required")).thenReturn(builder);
        assertFalse(new ISOOffsetDateTimeValidator().isValid(" ", context));
        verify(context).disableDefaultConstraintViolation();
        verify(builder).addConstraintViolation();
    }
    
    private static Stream<Arguments> assertIsValidCases() {
        return Stream.of(
                Arguments.of("without value", null, true),
                Arguments.of("with offset", "2026-10-12T10:00:00+08:00", true),
                Arguments.of("invalid date time", "foo_time", false));
    }
}
