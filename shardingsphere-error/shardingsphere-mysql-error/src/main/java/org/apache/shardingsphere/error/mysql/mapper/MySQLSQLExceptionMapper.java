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

import org.apache.shardingsphere.error.code.CommonErrorCode;
import org.apache.shardingsphere.error.code.SQLErrorCode;
import org.apache.shardingsphere.error.mapper.SQLExceptionMapper;
import org.apache.shardingsphere.error.mysql.code.MySQLServerErrorCode;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.exception.CircuitBreakException;
import org.apache.shardingsphere.infra.exception.DBCreateExistsException;
import org.apache.shardingsphere.infra.exception.DBDropNotExistsException;
import org.apache.shardingsphere.infra.exception.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.NoSuchTableException;
import org.apache.shardingsphere.infra.exception.ResourceNotExistedException;
import org.apache.shardingsphere.infra.exception.RuleNotExistedException;
import org.apache.shardingsphere.infra.exception.TableExistsException;
import org.apache.shardingsphere.infra.exception.TableLockWaitTimeoutException;
import org.apache.shardingsphere.infra.exception.TableLockedException;
import org.apache.shardingsphere.infra.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.exception.UnknownDatabaseException;
import org.apache.shardingsphere.infra.util.exception.ShardingSphereInsideException;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;

import java.sql.SQLException;

/**
 * SQL exception mapper for MySQL.
 */
public final class MySQLSQLExceptionMapper implements SQLExceptionMapper {
    
    @Override
    public SQLException convert(final ShardingSphereInsideException insideException) {
        if (insideException instanceof TableModifyInTransactionException) {
            return toSQLException(MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, ((TableModifyInTransactionException) insideException).getTableName());
        }
        if (insideException instanceof InsertColumnsAndValuesMismatchedException) {
            return toSQLException(MySQLServerErrorCode.ER_WRONG_VALUE_COUNT_ON_ROW, ((InsertColumnsAndValuesMismatchedException) insideException).getMismatchedRowNumber());
        }
        if (insideException instanceof UnknownDatabaseException) {
            return null != ((UnknownDatabaseException) insideException).getDatabaseName()
                    ? toSQLException(MySQLServerErrorCode.ER_BAD_DB_ERROR, ((UnknownDatabaseException) insideException).getDatabaseName())
                    : toSQLException(MySQLServerErrorCode.ER_NO_DB_ERROR);
        }
        if (insideException instanceof NoDatabaseSelectedException) {
            return toSQLException(MySQLServerErrorCode.ER_NO_DB_ERROR);
        }
        if (insideException instanceof DBCreateExistsException) {
            return toSQLException(MySQLServerErrorCode.ER_DB_CREATE_EXISTS_ERROR, ((DBCreateExistsException) insideException).getDatabaseName());
        }
        if (insideException instanceof DBDropNotExistsException) {
            return toSQLException(MySQLServerErrorCode.ER_DB_DROP_NOT_EXISTS_ERROR, ((DBDropNotExistsException) insideException).getDatabaseName());
        }
        if (insideException instanceof TableExistsException) {
            return toSQLException(MySQLServerErrorCode.ER_TABLE_EXISTS_ERROR, ((TableExistsException) insideException).getTableName());
        }
        if (insideException instanceof NoSuchTableException) {
            return toSQLException(MySQLServerErrorCode.ER_NO_SUCH_TABLE, ((NoSuchTableException) insideException).getTableName());
        }
        if (insideException instanceof CircuitBreakException) {
            return toSQLException(CommonErrorCode.CIRCUIT_BREAK_MODE);
        }
        if (insideException instanceof ShardingSphereConfigurationException || insideException instanceof SQLParsingException) {
            return toSQLException(MySQLServerErrorCode.ER_NOT_SUPPORTED_YET, insideException.getMessage());
        }
        if (insideException instanceof RuleNotExistedException || insideException instanceof ResourceNotExistedException) {
            return toSQLException(MySQLServerErrorCode.ER_SP_DOES_NOT_EXIST);
        }
        if (insideException instanceof TableLockWaitTimeoutException) {
            TableLockWaitTimeoutException exception = (TableLockWaitTimeoutException) insideException;
            return toSQLException(CommonErrorCode.TABLE_LOCK_WAIT_TIMEOUT, exception.getTableName(), exception.getSchemaName(), exception.getTimeoutMilliseconds());
        }
        if (insideException instanceof TableLockedException) {
            TableLockedException exception = (TableLockedException) insideException;
            return toSQLException(CommonErrorCode.TABLE_LOCKED, exception.getTableName(), exception.getSchemaName());
        }
        return toSQLException(CommonErrorCode.UNKNOWN_EXCEPTION, insideException.getMessage());
    }
    
    private SQLException toSQLException(final SQLErrorCode errorCode, final Object... messageArguments) {
        return new SQLException(String.format(errorCode.getErrorMessage(), messageArguments), errorCode.getSqlState(), errorCode.getErrorCode());
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
