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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ExcessTransactionsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.vendor.FirebirdVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;

import java.sql.SQLException;

/**
 * Firebird dialect exception mapper.
 */
public final class FirebirdDialectExceptionMapper implements SQLDialectExceptionMapper {
    
    @Override
    public SQLException convert(final SQLDialectException sqlDialectException) {
        if (sqlDialectException instanceof UnknownDatabaseException) {
            return toSQLException(FirebirdVendorError.UNAVAILABLE_DATABASE, ((UnknownDatabaseException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof DatabaseCreateExistsException) {
            return toSQLException(FirebirdVendorError.DATABASE_ALREADY_EXISTS, ((DatabaseCreateExistsException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof DatabaseDropNotExistsException) {
            return toSQLException(FirebirdVendorError.UNAVAILABLE_DATABASE, ((DatabaseDropNotExistsException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof AccessDeniedException) {
            return toSQLException(FirebirdVendorError.LOGIN_FAILED);
        }
        if (sqlDialectException instanceof InvalidBatchHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_BATCH_HANDLE);
        }
        if (sqlDialectException instanceof BatchTooBigException) {
            return toSQLException(FirebirdVendorError.BATCH_TOO_BIG);
        }
        if (sqlDialectException instanceof BatchAlreadyOpenedException) {
            return toSQLException(FirebirdVendorError.BATCH_ALREADY_OPENED);
        }
        if (sqlDialectException instanceof InvalidBatchParameterVersionException) {
            InvalidBatchParameterVersionException ex = (InvalidBatchParameterVersionException) sqlDialectException;
            return toSQLException(FirebirdVendorError.INVALID_BATCH_PARAMETER_VERSION, ex.getVersion(), ex.getExpectedVersion());
        }
        if (sqlDialectException instanceof BatchParametersRequiredException) {
            return toSQLException(FirebirdVendorError.BATCH_PARAMETERS_REQUIRED);
        }
        if (sqlDialectException instanceof InvalidBatchMessageFormatException) {
            return toSQLException(FirebirdVendorError.SQLDA_ERROR);
        }
        if (sqlDialectException instanceof InvalidStatementHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_STATEMENT_HANDLE);
        }
        if (sqlDialectException instanceof InvalidTransactionHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_TRANSACTION_HANDLE);
        }
        if (sqlDialectException instanceof ExcessTransactionsException) {
            return toSQLException(FirebirdVendorError.EXCESS_TRANSACTIONS, ((ExcessTransactionsException) sqlDialectException).getMaxTransactions());
        }
        if (sqlDialectException instanceof DialectSQLParsingException) {
            DialectSQLParsingException ex = (DialectSQLParsingException) sqlDialectException;
            return toSQLException(FirebirdVendorError.DYNAMIC_SQL_ERROR, ex.getMessage());
        }
        if (sqlDialectException instanceof TableExistsException) {
            return toSQLException(FirebirdVendorError.TABLE_ALREADY_EXISTS, ((TableExistsException) sqlDialectException).getTableName());
        }
        if (sqlDialectException instanceof InvalidParameterValueException) {
            return toSQLException(FirebirdVendorError.CHARSET_NOT_FOUND, ((InvalidParameterValueException) sqlDialectException).getParameterValue());
        }
        return new UnknownSQLException(sqlDialectException).toSQLException();
    }
    
    private SQLException toSQLException(final VendorError vendorError, final Object... messageArgs) {
        return new SQLException(String.format(vendorError.getReason(), messageArgs), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
