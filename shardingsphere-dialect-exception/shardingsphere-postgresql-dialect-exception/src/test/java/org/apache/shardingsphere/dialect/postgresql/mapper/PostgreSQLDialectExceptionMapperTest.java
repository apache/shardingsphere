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

package org.apache.shardingsphere.dialect.postgresql.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.dialect.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.dialect.exception.transaction.InTransactionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.postgresql.util.PSQLState;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class PostgreSQLDialectExceptionMapperTest {
    
    private final Class<SQLDialectException> sqlDialectExceptionClazz;
    
    private final String sqlState;
    
    @Parameters(name = "{1} -> {0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
                {DatabaseCreateExistsException.class, "42P04"},
                {InTransactionException.class, PSQLState.TRANSACTION_STATE_INVALID.getState()},
                {InsertColumnsAndValuesMismatchedException.class, PSQLState.SYNTAX_ERROR.getState()},
                {InvalidParameterValueException.class, PSQLState.INVALID_PARAMETER_VALUE.getState()},
                {TooManyConnectionsException.class, PSQLState.CONNECTION_REJECTED.getState()},
        });
    }
    
    @Test
    public void convert() {
        assertThat(new PostgreSQLDialectExceptionMapper().convert(mock(sqlDialectExceptionClazz)).getSQLState(), is(sqlState));
    }
}
