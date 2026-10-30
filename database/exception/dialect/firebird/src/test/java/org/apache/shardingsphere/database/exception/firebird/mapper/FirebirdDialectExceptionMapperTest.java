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

package org.apache.shardingsphere.database.exception.firebird.mapper;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.database.exception.core.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchAlreadyOpenedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchParametersRequiredException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.CannotUpdateOldBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ExcessTransactionsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrTypeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.vendor.FirebirdVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class FirebirdDialectExceptionMapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final SQLDialectExceptionMapper mapper = DatabaseTypedSPILoader.getService(SQLDialectExceptionMapper.class, databaseType);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertArguments")
    void assertConvert(final String name, final SQLDialectException sqlDialectException, final VendorError vendorError, final Object[] messageArgs) {
        SQLException actual = mapper.convert(sqlDialectException);
        assertThat(actual.getSQLState(), is(vendorError.getSqlState().getValue()));
        assertThat(actual.getErrorCode(), is(vendorError.getVendorCode()));
        assertThat(actual.getMessage(), is(String.format(vendorError.getReason(), messageArgs)));
    }
    
    @Test
    void assertConvertWithUnknownException() {
        assertThat(mapper.convert(mock(SQLDialectException.class)).getSQLState(), is("HY000"));
    }
    
    private static Stream<Arguments> convertArguments() {
        return Stream.of(
                Arguments.of("unknown_database", new UnknownDatabaseException("logic_db"), FirebirdVendorError.UNAVAILABLE_DATABASE, new Object[]{"logic_db"}),
                Arguments.of("database_create_exists", new DatabaseCreateExistsException("logic_db"), FirebirdVendorError.DATABASE_ALREADY_EXISTS, new Object[]{"logic_db"}),
                Arguments.of("database_drop_not_exists", new DatabaseDropNotExistsException("logic_db"), FirebirdVendorError.UNAVAILABLE_DATABASE, new Object[]{"logic_db"}),
                Arguments.of("access_denied", new AccessDeniedException("root", "127.0.0.1", true), FirebirdVendorError.LOGIN_FAILED, new Object[0]),
                Arguments.of("batch_too_big", new BatchTooBigException(42, 1L, 8L, 8L), FirebirdVendorError.BATCH_TOO_BIG, new Object[0]),
                Arguments.of("table_exists", new TableExistsException("t_order"), FirebirdVendorError.TABLE_ALREADY_EXISTS, new Object[]{"t_order"}),
                Arguments.of("dialect_sql_parsing", new DialectSQLParsingException("You have an error in your SQL syntax", "SELEC", 1), FirebirdVendorError.DYNAMIC_SQL_ERROR,
                        new Object[]{"You have an error in your SQL syntax"}),
                Arguments.of("invalid_batch_handle", new InvalidBatchHandleException(42), FirebirdVendorError.INVALID_BATCH_HANDLE, new Object[0]),
                Arguments.of("batch_already_opened", new BatchAlreadyOpenedException(42), FirebirdVendorError.BATCH_ALREADY_OPENED, new Object[0]),
                Arguments.of("invalid_batch_parameter_version", new InvalidBatchParameterVersionException(2, 1), FirebirdVendorError.INVALID_BATCH_PARAMETER_VERSION, new Object[]{2, 1}),
                Arguments.of("batch_parameters_required", new BatchParametersRequiredException(42), FirebirdVendorError.BATCH_PARAMETERS_REQUIRED, new Object[0]),
                Arguments.of("invalid_batch_message_format", new InvalidBatchMessageFormatException("invalid message length"), FirebirdVendorError.SQLDA_ERROR, new Object[0]),
                Arguments.of("invalid_statement_handle", new InvalidStatementHandleException(42), FirebirdVendorError.INVALID_STATEMENT_HANDLE, new Object[0]),
                Arguments.of("invalid_transaction_handle", new InvalidTransactionHandleException(42), FirebirdVendorError.INVALID_TRANSACTION_HANDLE, new Object[0]),
                Arguments.of("excess_transactions", new ExcessTransactionsException(1), FirebirdVendorError.EXCESS_TRANSACTIONS, new Object[]{1}),
                Arguments.of("invalid_parameter_value", new InvalidParameterValueException("names", "foo_charset"), FirebirdVendorError.CHARSET_NOT_FOUND, new Object[]{"foo_charset"}),
                Arguments.of("invalid_segstr_handle", new InvalidSegstrHandleException(42), FirebirdVendorError.INVALID_SEGSTR_HANDLE, new Object[0]),
                Arguments.of("invalid_segstr_id", new InvalidSegstrIdException(99L), FirebirdVendorError.INVALID_SEGSTR_ID, new Object[0]),
                Arguments.of("invalid_segstr_type", new InvalidSegstrTypeException(42), FirebirdVendorError.INVALID_SEGSTR_TYPE, new Object[0]),
                Arguments.of("cannot_update_old_blob", new CannotUpdateOldBlobException(42), FirebirdVendorError.CANNOT_UPDATE_OLD_BLOB, new Object[0]));
    }
}
