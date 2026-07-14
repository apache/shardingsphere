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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ExcessTransactionsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.vendor.FirebirdVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class FirebirdDialectExceptionMapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final SQLDialectExceptionMapper mapper = DatabaseTypedSPILoader.getService(SQLDialectExceptionMapper.class, databaseType);
    
    @Test
    void assertConvertWithUnknownDatabase() {
        assertSQLException(mapper.convert(new UnknownDatabaseException("logic_db")), FirebirdVendorError.UNAVAILABLE_DATABASE, "logic_db");
    }
    
    @Test
    void assertConvertWithDatabaseCreateExists() {
        assertSQLException(mapper.convert(new DatabaseCreateExistsException("logic_db")), FirebirdVendorError.DATABASE_ALREADY_EXISTS, "logic_db");
    }
    
    @Test
    void assertConvertWithDatabaseDropNotExists() {
        assertSQLException(mapper.convert(new DatabaseDropNotExistsException("logic_db")), FirebirdVendorError.UNAVAILABLE_DATABASE, "logic_db");
    }
    
    @Test
    void assertConvertWithAccessDenied() {
        assertSQLException(mapper.convert(new AccessDeniedException("root", "127.0.0.1", true)), FirebirdVendorError.LOGIN_FAILED);
    }
    
    @Test
    void assertConvertWithBatchTooBig() {
        assertSQLException(mapper.convert(new BatchTooBigException(42, 1L, 8L, 8L)), FirebirdVendorError.BATCH_TOO_BIG);
    }
    
    @Test
    void assertConvertWithTableExists() {
        assertSQLException(mapper.convert(new TableExistsException("t_order")), FirebirdVendorError.TABLE_ALREADY_EXISTS, "t_order");
    }
    
    @Test
    void assertConvertWithDialectSQLParsing() {
        assertSQLException(mapper.convert(new DialectSQLParsingException("You have an error in your SQL syntax", "SELEC", 1)), FirebirdVendorError.DYNAMIC_SQL_ERROR,
                "You have an error in your SQL syntax");
    }
    
    @Test
    void assertConvertWithInvalidBatchHandle() {
        assertSQLException(mapper.convert(new InvalidBatchHandleException(42)), FirebirdVendorError.INVALID_BATCH_HANDLE);
    }
    
    @Test
    void assertConvertWithBatchAlreadyOpened() {
        assertSQLException(mapper.convert(new BatchAlreadyOpenedException(42)), FirebirdVendorError.BATCH_ALREADY_OPENED);
    }
    
    @Test
    void assertConvertWithInvalidBatchParameterVersion() {
        assertSQLException(mapper.convert(new InvalidBatchParameterVersionException(2, 1)), FirebirdVendorError.INVALID_BATCH_PARAMETER_VERSION, 2);
    }
    
    @Test
    void assertConvertWithInvalidBatchMessageFormat() {
        assertSQLException(mapper.convert(new InvalidBatchMessageFormatException("invalid message length")), FirebirdVendorError.SQLDA_ERROR);
    }
    
    @Test
    void assertConvertWithInvalidStatementHandle() {
        assertSQLException(mapper.convert(new InvalidStatementHandleException(42)), FirebirdVendorError.INVALID_STATEMENT_HANDLE);
    }
    
    @Test
    void assertConvertWithInvalidTransactionHandle() {
        assertSQLException(mapper.convert(new InvalidTransactionHandleException(42)), FirebirdVendorError.INVALID_TRANSACTION_HANDLE);
    }
    
    @Test
    void assertConvertWithExcessTransactions() {
        assertSQLException(mapper.convert(new ExcessTransactionsException(1)), FirebirdVendorError.EXCESS_TRANSACTIONS, 1);
    }
    
    @Test
    void assertConvertWithInvalidParameterValue() {
        assertSQLException(mapper.convert(new InvalidParameterValueException("names", "foo_charset")), FirebirdVendorError.CHARSET_NOT_FOUND, "foo_charset");
    }
    
    @Test
    void assertConvertWithUnknownException() {
        SQLException actual = mapper.convert(mock(SQLDialectException.class));
        assertThat(actual.getSQLState(), is("HY000"));
    }
    
    private void assertSQLException(final SQLException actual, final VendorError vendorError, final Object... messageArgs) {
        assertThat(actual.getSQLState(), is(vendorError.getSqlState().getValue()));
        assertThat(actual.getErrorCode(), is(vendorError.getVendorCode()));
        assertThat(actual.getMessage(), is(String.format(vendorError.getReason(), messageArgs)));
    }
}
