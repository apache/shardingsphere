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

package org.apache.shardingsphere.proxy.frontend.mysql.err;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.error.CommonErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.CircuitBreakException;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.exception.DBDropExistsException;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.TableLockWaitTimeoutException;
import org.apache.shardingsphere.proxy.backend.exception.TableLockedException;
import org.apache.shardingsphere.proxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.CommonDistSQLErrorCode;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.CommonDistSQLException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedCommandException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;

import java.sql.SQLException;

/**
 * ERR packet factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLErrPacketFactory {
    
    /**
     * New instance of MySQL ERR packet.
     *
     * @param cause cause
     * @return instance of MySQL ERR packet
     */
    public static MySQLErrPacket newInstance(final Exception cause) {
        if (cause instanceof SQLException) {
            SQLException sqlException = (SQLException) cause;
            return null == sqlException.getSQLState() ? new MySQLErrPacket(1, MySQLServerErrorCode.ER_INTERNAL_ERROR, cause.getMessage())
                    : new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
        }
        if (cause instanceof CommonDistSQLException) {
            CommonDistSQLException commonDistSQLException = (CommonDistSQLException) cause;
            return new MySQLErrPacket(1, CommonDistSQLErrorCode.valueOf(commonDistSQLException), commonDistSQLException.getVariable());
        }
        if (cause instanceof TableModifyInTransactionException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, ((TableModifyInTransactionException) cause).getTableName());
        }
        if (cause instanceof UnknownDatabaseException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_BAD_DB_ERROR, ((UnknownDatabaseException) cause).getDatabaseName());
        }
        if (cause instanceof NoDatabaseSelectedException || cause instanceof SchemaNotExistedException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_NO_DB_ERROR);
        }
        if (cause instanceof DBCreateExistsException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_DB_CREATE_EXISTS_ERROR, ((DBCreateExistsException) cause).getDatabaseName());
        }
        if (cause instanceof DBDropExistsException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_DB_DROP_EXISTS_ERROR, ((DBDropExistsException) cause).getDatabaseName());
        }
        if (cause instanceof TableExistsException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_TABLE_EXISTS_ERROR, ((TableExistsException) cause).getTableName());
        }
        if (cause instanceof NoSuchTableException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_NO_SUCH_TABLE, ((NoSuchTableException) cause).getTableName());
        }
        if (cause instanceof CircuitBreakException) {
            return new MySQLErrPacket(1, CommonErrorCode.CIRCUIT_BREAK_MODE);
        }
        if (cause instanceof UnsupportedCommandException) {
            return new MySQLErrPacket(1, CommonErrorCode.UNSUPPORTED_COMMAND, ((UnsupportedCommandException) cause).getCommandType());
        }
        if (cause instanceof UnsupportedPreparedStatementException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_UNSUPPORTED_PS);
        }
        if (cause instanceof ShardingSphereConfigurationException || cause instanceof SQLParsingException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_NOT_SUPPORTED_YET, cause.getMessage());
        }
        if (cause instanceof RuleNotExistedException || cause instanceof DatabaseNotExistedException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_SP_DOES_NOT_EXIST);
        }
        if (cause instanceof TableLockWaitTimeoutException) {
            TableLockWaitTimeoutException exception = (TableLockWaitTimeoutException) cause;
            return new MySQLErrPacket(1, CommonErrorCode.TABLE_LOCK_WAIT_TIMEOUT, exception.getTableName(), 
                    exception.getSchemaName(), exception.getTimeoutMilliseconds());
        }
        if (cause instanceof TableLockedException) {
            TableLockedException exception = (TableLockedException) cause;
            return new MySQLErrPacket(1, CommonErrorCode.TABLE_LOCKED, exception.getTableName(),
                    exception.getSchemaName());
        }
        if (cause instanceof ScalingJobNotFoundException) {
            return new MySQLErrPacket(1, CommonErrorCode.SCALING_JOB_NOT_EXIST, ((ScalingJobNotFoundException) cause).getJobId());
        }
        if (cause instanceof RuntimeException) {
            return new MySQLErrPacket(1, CommonErrorCode.RUNTIME_EXCEPTION, cause.getMessage());
        }
        return new MySQLErrPacket(1, CommonErrorCode.UNKNOWN_EXCEPTION, cause.getMessage());
    }
}
