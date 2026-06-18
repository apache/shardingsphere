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

package org.apache.shardingsphere.database.exception.mysql.mapper;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.database.exception.core.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.database.exception.core.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.database.exception.core.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.database.exception.mysql.exception.DatabaseAccessDeniedException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorLocalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.HandshakeException;
import org.apache.shardingsphere.database.exception.mysql.exception.IncorrectGlobalLocalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.TooManyPlaceholdersException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCollationException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.database.exception.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;
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

class MySQLDialectExceptionMapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final SQLDialectExceptionMapper mapper = DatabaseTypedSPILoader.getService(SQLDialectExceptionMapper.class, databaseType);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertArguments")
    void assertConvert(final String name, final Class<? extends SQLDialectException> sqlDialectExceptionClass, final VendorError vendorError) {
        assertSQLException(mapper.convert(mock(sqlDialectExceptionClass)), vendorError);
    }
    
    private void assertSQLException(final SQLException actual, final VendorError vendorError) {
        assertThat(actual.getSQLState(), is(vendorError.getSqlState().getValue()));
        assertThat(actual.getErrorCode(), is(vendorError.getVendorCode()));
    }
    
    @Test
    void assertConvertWithUnknownDatabaseName() {
        assertSQLExceptionWithMessage(mapper.convert(new UnknownDatabaseException("logic_db")), MySQLVendorError.ER_BAD_DB_ERROR, "logic_db");
    }
    
    @Test
    void assertConvertWithUsingPassword() {
        assertSQLExceptionWithMessage(mapper.convert(new AccessDeniedException("root", "127.0.0.1", true)), MySQLVendorError.ER_ACCESS_DENIED_ERROR, "root", "127.0.0.1", "YES");
    }
    
    private void assertSQLExceptionWithMessage(final SQLException actual, final VendorError vendorError, final Object... messageArgs) {
        assertSQLException(actual, vendorError);
        assertThat(actual.getMessage(), is(String.format(vendorError.getReason(), messageArgs)));
    }
    
    @Test
    void assertConvertWithUnknownException() {
        SQLDialectException sqlDialectException = mock(SQLDialectException.class);
        SQLException expected = new UnknownSQLException(sqlDialectException).toSQLException();
        SQLException actual = mapper.convert(sqlDialectException);
        assertThat(actual.getMessage(), is(expected.getMessage()));
        assertThat(actual.getSQLState(), is(expected.getSQLState()));
        assertThat(actual.getErrorCode(), is(expected.getErrorCode()));
        assertThat(actual.getCause(), is(expected.getCause()));
    }
    
    private static Stream<Arguments> convertArguments() {
        return Stream.of(
                Arguments.of("unknown_database_without_name", UnknownDatabaseException.class, MySQLVendorError.ER_NO_DB_ERROR),
                Arguments.of("no_database_selected", NoDatabaseSelectedException.class, MySQLVendorError.ER_NO_DB_ERROR),
                Arguments.of("database_create_exists", DatabaseCreateExistsException.class, MySQLVendorError.ER_DB_CREATE_EXISTS_ERROR),
                Arguments.of("database_drop_not_exists", DatabaseDropNotExistsException.class, MySQLVendorError.ER_DB_DROP_NOT_EXISTS_ERROR),
                Arguments.of("table_exists", TableExistsException.class, MySQLVendorError.ER_TABLE_EXISTS_ERROR),
                Arguments.of("dialect_sql_parsing", DialectSQLParsingException.class, MySQLVendorError.ER_PARSE_ERROR),
                Arguments.of("no_such_table", NoSuchTableException.class, MySQLVendorError.ER_NO_SUCH_TABLE),
                Arguments.of("insert_columns_and_values_mismatched", InsertColumnsAndValuesMismatchedException.class, MySQLVendorError.ER_WRONG_VALUE_COUNT_ON_ROW),
                Arguments.of("table_modify_in_transaction", TableModifyInTransactionException.class, MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE),
                Arguments.of("too_many_connections", TooManyConnectionsException.class, MySQLVendorError.ER_CON_COUNT_ERROR),
                Arguments.of("unsupported_prepared_statement", UnsupportedPreparedStatementException.class, MySQLVendorError.ER_UNSUPPORTED_PS),
                Arguments.of("too_many_placeholders", TooManyPlaceholdersException.class, MySQLVendorError.ER_PS_MANY_PARAM),
                Arguments.of("unknown_charset", UnknownCharsetException.class, MySQLVendorError.ER_UNKNOWN_CHARACTER_SET),
                Arguments.of("unknown_collation", UnknownCollationException.class, MySQLVendorError.ER_UNKNOWN_COLLATION),
                Arguments.of("handshake", HandshakeException.class, MySQLVendorError.ER_HANDSHAKE_ERROR),
                Arguments.of("access_denied_without_password", AccessDeniedException.class, MySQLVendorError.ER_ACCESS_DENIED_ERROR),
                Arguments.of("database_access_denied", DatabaseAccessDeniedException.class, MySQLVendorError.ER_DBACCESS_DENIED_ERROR),
                Arguments.of("unknown_system_variable", UnknownSystemVariableException.class, MySQLVendorError.ER_UNKNOWN_SYSTEM_VARIABLE),
                Arguments.of("error_local_variable", ErrorLocalVariableException.class, MySQLVendorError.ER_LOCAL_VARIABLE),
                Arguments.of("error_global_variable", ErrorGlobalVariableException.class, MySQLVendorError.ER_GLOBAL_VARIABLE),
                Arguments.of("incorrect_global_local_variable", IncorrectGlobalLocalVariableException.class, MySQLVendorError.ER_INCORRECT_GLOBAL_LOCAL_VAR));
    }
}
