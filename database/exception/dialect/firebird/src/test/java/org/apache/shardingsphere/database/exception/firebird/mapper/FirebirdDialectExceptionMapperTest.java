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
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.database.exception.core.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBlobHandleException;
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
    void assertConvertWithInvalidBatchHandle() {
        assertSQLException(mapper.convert(new InvalidBatchHandleException(42)), FirebirdVendorError.INVALID_BATCH_HANDLE);
    }
    
    @Test
    void assertConvertWithInvalidBlobHandle() {
        assertSQLException(mapper.convert(new InvalidBlobHandleException(42)), FirebirdVendorError.INVALID_BLOB_HANDLE);
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
