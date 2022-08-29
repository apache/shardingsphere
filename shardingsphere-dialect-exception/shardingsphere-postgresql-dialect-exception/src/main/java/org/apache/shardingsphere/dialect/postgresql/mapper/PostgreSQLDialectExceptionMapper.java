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
import org.apache.shardingsphere.dialect.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.dialect.postgresql.exception.InvalidAuthorizationSpecificationException;
import org.apache.shardingsphere.dialect.postgresql.exception.PostgreSQLAuthenticationException;
import org.apache.shardingsphere.dialect.postgresql.exception.PostgreSQLProtocolViolationException;
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
        if (sqlDialectException instanceof DatabaseCreateExistsException) {
            return new PSQLException(PostgreSQLVendorError.DUPLICATE_DATABASE.getReason(), null);
        }
        if (sqlDialectException instanceof InTransactionException) {
            return new PSQLException(sqlDialectException.getMessage(), PSQLState.TRANSACTION_STATE_INVALID);
        }
        if (sqlDialectException instanceof InsertColumnsAndValuesMismatchedException) {
            return new PSQLException(sqlDialectException.getMessage(), PSQLState.SYNTAX_ERROR);
        }
        if (sqlDialectException instanceof InvalidParameterValueException) {
            InvalidParameterValueException invalidParameterValueException = (InvalidParameterValueException) sqlDialectException;
            String message = String.format("invalid value for parameter \"%s\": \"%s\"", invalidParameterValueException.getParameterName(), invalidParameterValueException.getParameterValue());
            return new PSQLException(message, PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (sqlDialectException instanceof TooManyConnectionsException) {
            return new PSQLException(PostgreSQLVendorError.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT.getReason(), null);
        }
        if (sqlDialectException instanceof InvalidAuthorizationSpecificationException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.INVALID_AUTHORIZATION_SPECIFICATION, "no PostgreSQL user name specified in startup packet"));
        }
        if (sqlDialectException instanceof PostgreSQLProtocolViolationException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.PROTOCOL_VIOLATION, sqlDialectException.getMessage()));
        }
        if (sqlDialectException instanceof PostgreSQLAuthenticationException) {
            return new PSQLException(ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.PROTOCOL_VIOLATION, sqlDialectException.getMessage()));
        }
        return new PSQLException(sqlDialectException.getMessage(), PSQLState.UNEXPECTED_ERROR);
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
