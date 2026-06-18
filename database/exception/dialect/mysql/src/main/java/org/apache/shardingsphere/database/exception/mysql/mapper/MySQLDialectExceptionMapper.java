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

import java.sql.SQLException;

/**
 * MySQL dialect exception mapper.
 */
public final class MySQLDialectExceptionMapper implements SQLDialectExceptionMapper {
    
    @Override
    public SQLException convert(final SQLDialectException sqlDialectException) {
        if (sqlDialectException instanceof UnknownDatabaseException) {
            return null != ((UnknownDatabaseException) sqlDialectException).getDatabaseName()
                    ? toSQLException(MySQLVendorError.ER_BAD_DB_ERROR, ((UnknownDatabaseException) sqlDialectException).getDatabaseName())
                    : toSQLException(MySQLVendorError.ER_NO_DB_ERROR);
        }
        if (sqlDialectException instanceof NoDatabaseSelectedException) {
            return toSQLException(MySQLVendorError.ER_NO_DB_ERROR);
        }
        if (sqlDialectException instanceof DatabaseCreateExistsException) {
            return toSQLException(MySQLVendorError.ER_DB_CREATE_EXISTS_ERROR, ((DatabaseCreateExistsException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof DatabaseDropNotExistsException) {
            return toSQLException(MySQLVendorError.ER_DB_DROP_NOT_EXISTS_ERROR, ((DatabaseDropNotExistsException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof TableExistsException) {
            return toSQLException(MySQLVendorError.ER_TABLE_EXISTS_ERROR, ((TableExistsException) sqlDialectException).getTableName());
        }
        if (sqlDialectException instanceof DialectSQLParsingException) {
            return toSQLException(MySQLVendorError.ER_PARSE_ERROR, sqlDialectException.getMessage(), ((DialectSQLParsingException) sqlDialectException).getSymbol(),
                    ((DialectSQLParsingException) sqlDialectException).getLine());
        }
        if (sqlDialectException instanceof NoSuchTableException) {
            return toSQLException(MySQLVendorError.ER_NO_SUCH_TABLE, ((NoSuchTableException) sqlDialectException).getTableName());
        }
        if (sqlDialectException instanceof InsertColumnsAndValuesMismatchedException) {
            return toSQLException(MySQLVendorError.ER_WRONG_VALUE_COUNT_ON_ROW, ((InsertColumnsAndValuesMismatchedException) sqlDialectException).getMismatchedRowNumber());
        }
        if (sqlDialectException instanceof TableModifyInTransactionException) {
            return toSQLException(MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, ((TableModifyInTransactionException) sqlDialectException).getTableName());
        }
        if (sqlDialectException instanceof TooManyConnectionsException) {
            return toSQLException(MySQLVendorError.ER_CON_COUNT_ERROR);
        }
        if (sqlDialectException instanceof UnsupportedPreparedStatementException) {
            return toSQLException(MySQLVendorError.ER_UNSUPPORTED_PS);
        }
        if (sqlDialectException instanceof TooManyPlaceholdersException) {
            return toSQLException(MySQLVendorError.ER_PS_MANY_PARAM);
        }
        if (sqlDialectException instanceof UnknownCharsetException) {
            return toSQLException(MySQLVendorError.ER_UNKNOWN_CHARACTER_SET, ((UnknownCharsetException) sqlDialectException).getCharset());
        }
        if (sqlDialectException instanceof UnknownCollationException) {
            return toSQLException(MySQLVendorError.ER_UNKNOWN_COLLATION, ((UnknownCollationException) sqlDialectException).getCollationId());
        }
        if (sqlDialectException instanceof HandshakeException) {
            return toSQLException(MySQLVendorError.ER_HANDSHAKE_ERROR);
        }
        if (sqlDialectException instanceof AccessDeniedException) {
            AccessDeniedException ex = (AccessDeniedException) sqlDialectException;
            return toSQLException(MySQLVendorError.ER_ACCESS_DENIED_ERROR, ex.getUsername(), ex.getHostname(), ex.isUsingPassword() ? "YES" : "NO");
        }
        if (sqlDialectException instanceof DatabaseAccessDeniedException) {
            DatabaseAccessDeniedException ex = (DatabaseAccessDeniedException) sqlDialectException;
            return toSQLException(MySQLVendorError.ER_DBACCESS_DENIED_ERROR, ex.getUsername(), ex.getHostname(), ex.getDatabaseName());
        }
        if (sqlDialectException instanceof UnknownSystemVariableException) {
            return toSQLException(MySQLVendorError.ER_UNKNOWN_SYSTEM_VARIABLE, ((UnknownSystemVariableException) sqlDialectException).getVariableName());
        }
        if (sqlDialectException instanceof ErrorLocalVariableException) {
            return toSQLException(MySQLVendorError.ER_LOCAL_VARIABLE, ((ErrorLocalVariableException) sqlDialectException).getVariableName());
        }
        if (sqlDialectException instanceof ErrorGlobalVariableException) {
            return toSQLException(MySQLVendorError.ER_GLOBAL_VARIABLE, ((ErrorGlobalVariableException) sqlDialectException).getVariableName());
        }
        if (sqlDialectException instanceof IncorrectGlobalLocalVariableException) {
            IncorrectGlobalLocalVariableException ex = (IncorrectGlobalLocalVariableException) sqlDialectException;
            return toSQLException(MySQLVendorError.ER_INCORRECT_GLOBAL_LOCAL_VAR, ex.getVariableName(), ex.getScope());
        }
        return new UnknownSQLException(sqlDialectException).toSQLException();
    }
    
    private SQLException toSQLException(final VendorError vendorError, final Object... messageArgs) {
        return new SQLException(String.format(vendorError.getReason(), messageArgs), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
