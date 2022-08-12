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

package org.apache.shardingsphere.error.postgresql.mapper;

import org.apache.shardingsphere.error.mapper.SQLExceptionMapper;
import org.apache.shardingsphere.error.postgresql.code.PostgreSQLErrorCode;
import org.apache.shardingsphere.infra.exception.dialect.DBCreateExistsException;
import org.apache.shardingsphere.infra.exception.dialect.InTransactionException;
import org.apache.shardingsphere.infra.exception.dialect.InvalidParameterValueException;
import org.apache.shardingsphere.infra.exception.dialect.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.util.exception.inside.InsideDialectSQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.sql.SQLException;

/**
 * SQL exception mapper for PostgreSQL.
 */
public final class PostgreSQLSQLExceptionMapper implements SQLExceptionMapper {
    
    @Override
    public SQLException convert(final InsideDialectSQLException dialectSQLException) {
        if (dialectSQLException instanceof InTransactionException) {
            return new PSQLException(dialectSQLException.getMessage(), PSQLState.TRANSACTION_STATE_INVALID);
        }
        if (dialectSQLException instanceof InsertColumnsAndValuesMismatchedException) {
            return new PSQLException(dialectSQLException.getMessage(), PSQLState.SYNTAX_ERROR);
        }
        if (dialectSQLException instanceof InvalidParameterValueException) {
            InvalidParameterValueException invalidParameterValueException = (InvalidParameterValueException) dialectSQLException;
            String message = String.format("invalid value for parameter \"%s\": \"%s\"", invalidParameterValueException.getParameterName(), invalidParameterValueException.getParameterValue());
            return new PSQLException(message, PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (dialectSQLException instanceof DBCreateExistsException) {
            return new PSQLException(PostgreSQLErrorCode.DUPLICATE_DATABASE.getConditionName(), null);
        }
        return new PSQLException(dialectSQLException.getMessage(), null);
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
