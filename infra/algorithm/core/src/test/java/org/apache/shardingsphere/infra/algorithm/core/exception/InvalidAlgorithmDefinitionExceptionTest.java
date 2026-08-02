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

package org.apache.shardingsphere.infra.algorithm.core.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class InvalidAlgorithmDefinitionExceptionTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("constructorArguments")
    void assertConstruct(final String name, final Supplier<InvalidAlgorithmDefinitionException> exceptionSupplier, final String expectedMessage) {
        SQLException actual = exceptionSupplier.get().toSQLException();
        assertThat(actual.getSQLState(), is("44000"));
        assertThat(actual.getErrorCode(), is(10404));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Stream<Arguments> constructorArguments() {
        return Stream.of(
                Arguments.of("with algorithm", (Supplier<InvalidAlgorithmDefinitionException>) () -> new InvalidAlgorithmDefinitionException("sharding", "foo_algorithm"),
                        "Invalid sharding algorithm configuration 'foo_algorithm'."),
                Arguments.of("without algorithm", (Supplier<InvalidAlgorithmDefinitionException>) () -> new InvalidAlgorithmDefinitionException("sharding"),
                        "Invalid sharding algorithm configuration."),
                Arguments.of("with message", (Supplier<InvalidAlgorithmDefinitionException>) () -> new InvalidAlgorithmDefinitionException("sharding", "foo_algorithm", "Invalid properties"),
                        "Invalid sharding algorithm configuration 'foo_algorithm'. Invalid properties."));
    }
}
