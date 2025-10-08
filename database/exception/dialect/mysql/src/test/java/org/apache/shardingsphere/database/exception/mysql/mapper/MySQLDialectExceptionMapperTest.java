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
import org.apache.shardingsphere.database.exception.mysql.exception.DatabaseAccessDeniedException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorLocalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.HandshakeException;
import org.apache.shardingsphere.database.exception.mysql.exception.IncorrectGlobalLocalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCollationException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.database.exception.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class MySQLDialectExceptionMapperTest {
    
    @ParameterizedTest(name = "{1} -> {0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertConvert(final Class<SQLDialectException> sqlDialectExceptionClazz, final VendorError vendorError) {
        SQLException actual = new MySQLDialectExceptionMapper().convert(mock(sqlDialectExceptionClazz));
        assertThat(actual.getSQLState(), is(vendorError.getSqlState().getValue()));
        assertThat(actual.getErrorCode(), is(vendorError.getVendorCode()));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(Arguments.of(UnknownDatabaseException.class, MySQLVendorError.ER_NO_DB_ERROR),
                    Arguments.of(NoDatabaseSelectedException.class, MySQLVendorError.ER_NO_DB_ERROR),
                    Arguments.of(DatabaseCreateExistsException.class, MySQLVendorError.ER_DB_CREATE_EXISTS_ERROR),
                    Arguments.of(DatabaseDropNotExistsException.class, MySQLVendorError.ER_DB_DROP_NOT_EXISTS_ERROR),
                    Arguments.of(TableExistsException.class, MySQLVendorError.ER_TABLE_EXISTS_ERROR),
                    Arguments.of(DialectSQLParsingException.class, MySQLVendorError.ER_PARSE_ERROR),
                    Arguments.of(NoSuchTableException.class, MySQLVendorError.ER_NO_SUCH_TABLE),
                    Arguments.of(InsertColumnsAndValuesMismatchedException.class, MySQLVendorError.ER_WRONG_VALUE_COUNT_ON_ROW),
                    Arguments.of(TableModifyInTransactionException.class, MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE),
                    Arguments.of(TooManyConnectionsException.class, MySQLVendorError.ER_CON_COUNT_ERROR),
                    Arguments.of(UnsupportedPreparedStatementException.class, MySQLVendorError.ER_UNSUPPORTED_PS),
                    Arguments.of(UnknownCharsetException.class, MySQLVendorError.ER_UNKNOWN_CHARACTER_SET),
                    Arguments.of(UnknownCollationException.class, MySQLVendorError.ER_UNKNOWN_COLLATION),
                    Arguments.of(HandshakeException.class, MySQLVendorError.ER_HANDSHAKE_ERROR),
                    Arguments.of(AccessDeniedException.class, MySQLVendorError.ER_ACCESS_DENIED_ERROR),
                    Arguments.of(DatabaseAccessDeniedException.class, MySQLVendorError.ER_DBACCESS_DENIED_ERROR),
                    Arguments.of(UnknownSystemVariableException.class, MySQLVendorError.ER_UNKNOWN_SYSTEM_VARIABLE),
                    Arguments.of(ErrorLocalVariableException.class, MySQLVendorError.ER_LOCAL_VARIABLE),
                    Arguments.of(ErrorGlobalVariableException.class, MySQLVendorError.ER_GLOBAL_VARIABLE),
                    Arguments.of(IncorrectGlobalLocalVariableException.class, MySQLVendorError.ER_INCORRECT_GLOBAL_LOCAL_VAR));
        }
    }
}
