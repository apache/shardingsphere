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
import org.apache.shardingsphere.database.exception.postgresql.exception.PostgreSQLException.ServerErrorMessage;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.EmptyUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.PrivilegeNotGrantedException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.protocol.ProtocolViolationException;
import org.apache.shardingsphere.database.exception.postgresql.sqlstate.PostgreSQLState;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;

import java.sql.SQLException;

/**
 * PostgreSQL dialect exception mapper.
 */
public final class PostgreSQLDialectExceptionMapper implements SQLDialectExceptionMapper {
    
    private static final String FATAL_SEVERITY = "FATAL";
    
    private static final String ERROR_SEVERITY = "ERROR";
    
    @Override
    public SQLException convert(final SQLDialectException sqlDialectException) {
        if (sqlDialectException instanceof UnknownDatabaseException) {
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.INVALID_CATALOG_NAME, ((UnknownDatabaseException) sqlDialectException).getDatabaseName()));
        }
        if (sqlDialectException instanceof DatabaseCreateExistsException) {
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.DUPLICATE_DATABASE, ((DatabaseCreateExistsException) sqlDialectException).getDatabaseName()));
        }
        if (sqlDialectException instanceof NoSuchTableException) {
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.NO_SUCH_TABLE, ((NoSuchTableException) sqlDialectException).getTableName()));
        }
        if (sqlDialectException instanceof TableExistsException) {
            return new PostgreSQLException(new ServerErrorMessage(ERROR_SEVERITY, PostgreSQLVendorError.DUPLICATE_TABLE, ((TableExistsException) sqlDialectException).getTableName()));
        }
        if (sqlDialectException instanceof InTransactionException) {
            return new PostgreSQLException(new ServerErrorMessage(ERROR_SEVERITY, PostgreSQLVendorError.TRANSACTION_STATE_INVALID));
        }
        if (sqlDialectException instanceof InsertColumnsAndValuesMismatchedException) {
            return new PostgreSQLException(new ServerErrorMessage(ERROR_SEVERITY,
                    PostgreSQLVendorError.WRONG_VALUE_COUNT_ON_ROW, ((InsertColumnsAndValuesMismatchedException) sqlDialectException).getMismatchedRowNumber()));
        }
        if (sqlDialectException instanceof InvalidParameterValueException) {
            InvalidParameterValueException cause = (InvalidParameterValueException) sqlDialectException;
            return new PostgreSQLException(new ServerErrorMessage(ERROR_SEVERITY, PostgreSQLVendorError.INVALID_PARAMETER_VALUE, cause.getParameterName(), cause.getParameterValue()));
        }
        if (sqlDialectException instanceof TooManyConnectionsException) {
            return new PostgreSQLException(new ServerErrorMessage(ERROR_SEVERITY, PostgreSQLVendorError.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT));
        }
        if (sqlDialectException instanceof UnknownUsernameException) {
            return new PostgreSQLException(new ServerErrorMessage(
                    FATAL_SEVERITY, PostgreSQLVendorError.INVALID_AUTHORIZATION_SPECIFICATION, ((UnknownUsernameException) sqlDialectException).getUsername()));
        }
        if (sqlDialectException instanceof InvalidPasswordException) {
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.INVALID_PASSWORD, ((InvalidPasswordException) sqlDialectException).getUsername()));
        }
        if (sqlDialectException instanceof PrivilegeNotGrantedException) {
            PrivilegeNotGrantedException cause = (PrivilegeNotGrantedException) sqlDialectException;
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.PRIVILEGE_NOT_GRANTED, cause.getUsername(), cause.getDatabaseName()));
        }
        if (sqlDialectException instanceof EmptyUsernameException) {
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.NO_USERNAME));
        }
        if (sqlDialectException instanceof ProtocolViolationException) {
            ProtocolViolationException cause = (ProtocolViolationException) sqlDialectException;
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.PROTOCOL_VIOLATION, cause.getExpectedMessageType(), cause.getActualMessageType()));
        }
        if (sqlDialectException instanceof ColumnNotFoundException) {
            ColumnNotFoundException cause = (ColumnNotFoundException) sqlDialectException;
            return new PostgreSQLException(new ServerErrorMessage(FATAL_SEVERITY, PostgreSQLVendorError.UNDEFINED_COLUMN, cause.getTableName(), cause.getColumnName()));
        }
        return new PostgreSQLException(sqlDialectException.getMessage(), PostgreSQLState.UNEXPECTED_ERROR.getValue());
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
