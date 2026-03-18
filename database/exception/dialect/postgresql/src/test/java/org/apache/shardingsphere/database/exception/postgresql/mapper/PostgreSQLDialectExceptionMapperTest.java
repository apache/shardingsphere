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

package org.apache.shardingsphere.database.exception.postgresql.mapper;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.database.exception.core.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.exception.core.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.database.exception.postgresql.exception.PostgreSQLException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.EmptyUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.PrivilegeNotGrantedException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.protocol.ProtocolViolationException;
import org.apache.shardingsphere.database.exception.postgresql.sqlstate.PostgreSQLState;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLDialectExceptionMapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final SQLDialectExceptionMapper mapper = DatabaseTypedSPILoader.getService(SQLDialectExceptionMapper.class, databaseType);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertArguments")
    void assertConvert(final String name, final Class<? extends SQLDialectException> sqlDialectExceptionClass, final VendorError vendorError, final String severity) {
        PostgreSQLException actual = (PostgreSQLException) mapper.convert(mock(sqlDialectExceptionClass));
        assertThat(actual.getSQLState(), is(vendorError.getSqlState().getValue()));
        assertNotNull(actual.getServerErrorMessage());
        assertThat(actual.getServerErrorMessage().getSqlState(), is(vendorError.getSqlState().getValue()));
        assertThat(actual.getServerErrorMessage().getSeverity(), is(severity));
    }
    
    @Test
    void assertConvertWithUnexpectedException() {
        SQLDialectException sqlDialectException = mock(SQLDialectException.class);
        when(sqlDialectException.getMessage()).thenReturn("unexpected");
        PostgreSQLException actual = (PostgreSQLException) mapper.convert(sqlDialectException);
        assertThat(actual.getMessage(), is("unexpected"));
        assertThat(actual.getSQLState(), is(PostgreSQLState.UNEXPECTED_ERROR.getValue()));
        assertNull(actual.getServerErrorMessage());
    }
    
    private static Stream<Arguments> convertArguments() {
        return Stream.of(
                Arguments.of("unknown_database", UnknownDatabaseException.class, PostgreSQLVendorError.INVALID_CATALOG_NAME, "FATAL"),
                Arguments.of("database_create_exists", DatabaseCreateExistsException.class, PostgreSQLVendorError.DUPLICATE_DATABASE, "FATAL"),
                Arguments.of("no_such_table", NoSuchTableException.class, PostgreSQLVendorError.NO_SUCH_TABLE, "FATAL"),
                Arguments.of("table_exists", TableExistsException.class, PostgreSQLVendorError.DUPLICATE_TABLE, "ERROR"),
                Arguments.of("in_transaction", InTransactionException.class, PostgreSQLVendorError.TRANSACTION_STATE_INVALID, "ERROR"),
                Arguments.of("insert_columns_and_values_mismatched", InsertColumnsAndValuesMismatchedException.class, PostgreSQLVendorError.WRONG_VALUE_COUNT_ON_ROW, "ERROR"),
                Arguments.of("invalid_parameter_value", InvalidParameterValueException.class, PostgreSQLVendorError.INVALID_PARAMETER_VALUE, "ERROR"),
                Arguments.of("too_many_connections", TooManyConnectionsException.class, PostgreSQLVendorError.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT, "ERROR"),
                Arguments.of("unknown_username", UnknownUsernameException.class, PostgreSQLVendorError.INVALID_AUTHORIZATION_SPECIFICATION, "FATAL"),
                Arguments.of("invalid_password", InvalidPasswordException.class, PostgreSQLVendorError.INVALID_PASSWORD, "FATAL"),
                Arguments.of("privilege_not_granted", PrivilegeNotGrantedException.class, PostgreSQLVendorError.PRIVILEGE_NOT_GRANTED, "FATAL"),
                Arguments.of("empty_username", EmptyUsernameException.class, PostgreSQLVendorError.NO_USERNAME, "FATAL"),
                Arguments.of("protocol_violation", ProtocolViolationException.class, PostgreSQLVendorError.PROTOCOL_VIOLATION, "FATAL"),
                Arguments.of("column_not_found", ColumnNotFoundException.class, PostgreSQLVendorError.UNDEFINED_COLUMN, "FATAL"));
    }
}
