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

package org.apache.shardingsphere.dialect.mysql.mapper;

import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.dialect.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.dialect.mysql.exception.UnknownCollationException;
import org.apache.shardingsphere.dialect.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.dialect.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.dialect.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.util.exception.sql.UnknownSQLException;
import org.apache.shardingsphere.infra.util.exception.sql.vendor.VendorError;

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
        if (sqlDialectException instanceof UnknownCharsetException) {
            return toSQLException(MySQLVendorError.ER_UNKNOWN_CHARACTER_SET, ((UnknownCharsetException) sqlDialectException).getCharset());
        }
        if (sqlDialectException instanceof UnknownCollationException) {
            return toSQLException(MySQLVendorError.ER_UNKNOWN_COLLATION, ((UnknownCollationException) sqlDialectException).getCollationId());
        }
        return new UnknownSQLException(sqlDialectException).toSQLException();
    }
    
    private SQLException toSQLException(final VendorError vendorError, final Object... messageArguments) {
        return new SQLException(String.format(vendorError.getReason(), messageArguments), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
