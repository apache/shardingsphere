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

package org.apache.shardingsphere.infra.exception.postgresql.mapper;

import org.apache.shardingsphere.infra.exception.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.infra.exception.dialect.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.infra.exception.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.exception.dialect.exception.transaction.InTransactionException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.postgresql.util.PSQLState;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class PostgreSQLDialectExceptionMapperTest {
    
    @ParameterizedTest(name = "{1} -> {0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertConvert(final Class<SQLDialectException> sqlDialectExceptionClazz, final String sqlState) {
        assertThat(new PostgreSQLDialectExceptionMapper().convert(mock(sqlDialectExceptionClazz)).getSQLState(), is(sqlState));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of(DatabaseCreateExistsException.class, "42P04"),
                    Arguments.of(InTransactionException.class, PSQLState.TRANSACTION_STATE_INVALID.getState()),
                    Arguments.of(InsertColumnsAndValuesMismatchedException.class, PSQLState.SYNTAX_ERROR.getState()),
                    Arguments.of(InvalidParameterValueException.class, PSQLState.INVALID_PARAMETER_VALUE.getState()),
                    Arguments.of(TooManyConnectionsException.class, PSQLState.CONNECTION_REJECTED.getState()));
        }
    }
}
