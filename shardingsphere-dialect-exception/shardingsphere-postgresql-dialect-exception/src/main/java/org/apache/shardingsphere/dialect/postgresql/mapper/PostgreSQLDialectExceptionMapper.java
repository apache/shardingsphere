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
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.exception.transaction.InTransactionException;
import org.apache.shardingsphere.dialect.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.dialect.postgresql.exception.InvalidAuthorizationSpecificationException;
import org.apache.shardingsphere.dialect.postgresql.exception.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.PostgreSQLProtocolViolationException;
import org.apache.shardingsphere.dialect.postgresql.exception.PrivilegeNotGrantedException;
import org.apache.shardingsphere.dialect.postgresql.exception.UnknownUsernameException;
import org.apache.shardingsphere.dialect.postgresql.message.ServerErrorMessageBuilder;
import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.sql.SQLException;

/**
 * PostgreSQL dialect exception mapper.
 */
public final class PostgreSQLDialectExceptionMapper implements SQLDialectExceptionMapper {
    
    @Override
    public SQLException convert(final SQLDialectException sqlDialectException) {
        if (sqlDialectException instanceof UnknownDatabaseException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.INVALID_CATALOG_NAME, ((UnknownDatabaseException) sqlDialectException).getDatabaseName()));
        }
        if (sqlDialectException instanceof UnknownUsernameException) {
            return new PSQLException(ServerErrorMessageBuilder.build(
                    "FATAL", PostgreSQLVendorError.INVALID_AUTHORIZATION_SPECIFICATION, ((UnknownUsernameException) sqlDialectException).getUsername()));
        }
        if (sqlDialectException instanceof InvalidPasswordException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.INVALID_PASSWORD, ((InvalidPasswordException) sqlDialectException).getUsername()));
        }
        if (sqlDialectException instanceof PrivilegeNotGrantedException) {
            PrivilegeNotGrantedException cause = (PrivilegeNotGrantedException) sqlDialectException;
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.PRIVILEGE_NOT_GRANTED, cause.getUsername(), cause.getDatabaseName()));
        }
        if (sqlDialectException instanceof DatabaseCreateExistsException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.DUPLICATE_DATABASE, ((DatabaseCreateExistsException) sqlDialectException).getDatabaseName()));
        }
        if (sqlDialectException instanceof InTransactionException) {
            return new PSQLException(ServerErrorMessageBuilder.build("ERROR", PostgreSQLVendorError.TRANSACTION_STATE_INVALID));
        }
        if (sqlDialectException instanceof InsertColumnsAndValuesMismatchedException) {
            return new PSQLException(ServerErrorMessageBuilder.build("ERROR",
                    PostgreSQLVendorError.WRONG_VALUE_COUNT_ON_ROW, ((InsertColumnsAndValuesMismatchedException) sqlDialectException).getMismatchedRowNumber()));
        }
        if (sqlDialectException instanceof InvalidParameterValueException) {
            InvalidParameterValueException cause = (InvalidParameterValueException) sqlDialectException;
            String message = String.format("invalid value for parameter \"%s\": \"%s\"", cause.getParameterName(), cause.getParameterValue());
            return new PSQLException(message, PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (sqlDialectException instanceof TooManyConnectionsException) {
            return new PSQLException(ServerErrorMessageBuilder.build("ERROR", PostgreSQLVendorError.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT));
        }
        if (sqlDialectException instanceof InvalidAuthorizationSpecificationException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.NO_USERNAME));
        }
        if (sqlDialectException instanceof PostgreSQLProtocolViolationException) {
            PostgreSQLProtocolViolationException cause = (PostgreSQLProtocolViolationException) sqlDialectException;
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.PROTOCOL_VIOLATION, cause.getExpectedMessageType(), cause.getActualMessageType()));
        }
        return new PSQLException(sqlDialectException.getMessage(), PSQLState.UNEXPECTED_ERROR);
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
