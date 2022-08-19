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

import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.dialect.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.dialect.exception.transaction.InTransactionException;
import org.junit.Test;
import org.postgresql.util.PSQLState;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class PostgreSQLDialectExceptionMapperTest {
    
    private Collection<Object[]> getConvertParameters() {
        return Arrays.asList(new Object[][]{
                {DatabaseCreateExistsException.class, null},
                {InTransactionException.class, PSQLState.TRANSACTION_STATE_INVALID},
                {InsertColumnsAndValuesMismatchedException.class, PSQLState.SYNTAX_ERROR},
                {InvalidParameterValueException.class, PSQLState.INVALID_PARAMETER_VALUE},
                {TooManyConnectionsException.class, null},
        });
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void convert() {
        PostgreSQLDialectExceptionMapper postgreSQLDialect = new PostgreSQLDialectExceptionMapper();
        for (Object[] item : getConvertParameters()) {
            assertThat(postgreSQLDialect.convert(mock((Class<SQLDialectException>) item[0])).getSQLState(), is(item[1] == null ? null : ((PSQLState) item[1]).getState()));
        }
    }
}
