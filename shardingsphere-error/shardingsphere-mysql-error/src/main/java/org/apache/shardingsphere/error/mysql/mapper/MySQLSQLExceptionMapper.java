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

package org.apache.shardingsphere.error.mysql.mapper;

import org.apache.shardingsphere.error.code.SQLErrorCode;
import org.apache.shardingsphere.error.code.StandardSQLErrorCode;
import org.apache.shardingsphere.error.mapper.SQLExceptionMapper;
import org.apache.shardingsphere.error.mysql.code.MySQLServerErrorCode;
import org.apache.shardingsphere.infra.exception.dialect.DBCreateExistsException;
import org.apache.shardingsphere.infra.exception.dialect.DBDropNotExistsException;
import org.apache.shardingsphere.infra.exception.dialect.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.exception.dialect.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.NoSuchTableException;
import org.apache.shardingsphere.infra.exception.dialect.TableExistsException;
import org.apache.shardingsphere.infra.exception.dialect.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.exception.dialect.TooManyConnectionsException;
import org.apache.shardingsphere.infra.exception.dialect.UnknownDatabaseException;
import org.apache.shardingsphere.infra.util.exception.inside.InsideDialectSQLException;

import java.sql.SQLException;

/**
 * SQL exception mapper for MySQL.
 */
public final class MySQLSQLExceptionMapper implements SQLExceptionMapper {
    
    @Override
    public SQLException convert(final InsideDialectSQLException dialectSQLException) {
        if (dialectSQLException instanceof TableModifyInTransactionException) {
            return toSQLException(MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, ((TableModifyInTransactionException) dialectSQLException).getTableName());
        }
        if (dialectSQLException instanceof InsertColumnsAndValuesMismatchedException) {
            return toSQLException(MySQLServerErrorCode.ER_WRONG_VALUE_COUNT_ON_ROW, ((InsertColumnsAndValuesMismatchedException) dialectSQLException).getMismatchedRowNumber());
        }
        if (dialectSQLException instanceof UnknownDatabaseException) {
            return null != ((UnknownDatabaseException) dialectSQLException).getDatabaseName()
                    ? toSQLException(MySQLServerErrorCode.ER_BAD_DB_ERROR, ((UnknownDatabaseException) dialectSQLException).getDatabaseName())
                    : toSQLException(MySQLServerErrorCode.ER_NO_DB_ERROR);
        }
        if (dialectSQLException instanceof NoDatabaseSelectedException) {
            return toSQLException(MySQLServerErrorCode.ER_NO_DB_ERROR);
        }
        if (dialectSQLException instanceof DBCreateExistsException) {
            return toSQLException(MySQLServerErrorCode.ER_DB_CREATE_EXISTS_ERROR, ((DBCreateExistsException) dialectSQLException).getDatabaseName());
        }
        if (dialectSQLException instanceof DBDropNotExistsException) {
            return toSQLException(MySQLServerErrorCode.ER_DB_DROP_NOT_EXISTS_ERROR, ((DBDropNotExistsException) dialectSQLException).getDatabaseName());
        }
        if (dialectSQLException instanceof TableExistsException) {
            return toSQLException(MySQLServerErrorCode.ER_TABLE_EXISTS_ERROR, ((TableExistsException) dialectSQLException).getTableName());
        }
        if (dialectSQLException instanceof NoSuchTableException) {
            return toSQLException(MySQLServerErrorCode.ER_NO_SUCH_TABLE, ((NoSuchTableException) dialectSQLException).getTableName());
        }
        if (dialectSQLException instanceof TooManyConnectionsException) {
            return toSQLException(MySQLServerErrorCode.ER_CON_COUNT_ERROR);
        }
        return toSQLException(StandardSQLErrorCode.UNKNOWN_EXCEPTION, dialectSQLException.getMessage());
    }
    
    private SQLException toSQLException(final SQLErrorCode errorCode, final Object... messageArguments) {
        return new SQLException(String.format(errorCode.getErrorMessage(), messageArguments), errorCode.getSqlState(), errorCode.getErrorCode());
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
