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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class FirebirdServerPreparedStatementTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertExactNumericParametersArguments")
    void assertConvertExactNumericParameters(final String name, final Object param, final Object expected) {
        FirebirdServerPreparedStatement preparedStatement = createPreparedStatement();
        preparedStatement.getExactNumericParameterIndexes().add(0);
        List<Object> actual = new ArrayList<>(Collections.singletonList(param));
        preparedStatement.convertExactNumericParameters(actual);
        assertThat(actual, is(Collections.singletonList(expected)));
    }
    
    private FirebirdServerPreparedStatement createPreparedStatement() {
        return new FirebirdServerPreparedStatement("SELECT id FROM foo_tbl WHERE amount = ?", mock(SQLStatementContext.class), new HintValueContext());
    }
    
    private static Stream<Arguments> convertExactNumericParametersArguments() {
        return Stream.of(
                Arguments.of("integral", "10", 10L),
                Arguments.of("integral with zero fraction", "10.00", 10L),
                Arguments.of("fractional", "12.34", new BigDecimal("12.34")),
                Arguments.of("over long range", "12345678901234567890", new BigDecimal("12345678901234567890")),
                Arguments.of("not a number", "foo_value", "foo_value"));
    }
    
    @Test
    void assertConvertExactNumericParametersWithOtherParameters() {
        FirebirdServerPreparedStatement preparedStatement = createPreparedStatement();
        preparedStatement.getExactNumericParameterIndexes().addAll(Arrays.asList(1, 2));
        List<Object> actual = new ArrayList<>(Arrays.asList("10", null));
        preparedStatement.convertExactNumericParameters(actual);
        assertThat(actual, is(Arrays.asList("10", null)));
    }
}
