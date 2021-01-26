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

package org.apache.shardingsphere.proxy.frontend.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.error.CommonErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.proxy.backend.exception.AddReplicaQueryRuleDataSourcesExistedException;
import org.apache.shardingsphere.proxy.backend.exception.CircuitBreakException;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.exception.DBDropExistsException;
import org.apache.shardingsphere.proxy.backend.exception.LockWaitTimeoutException;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleDataSourcesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleCreateExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceInUsedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.exception.TablesInUsedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.text.sctl.ShardingCTLErrorCode;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.ShardingCTLException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedCommandException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
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
            return null != sqlException.getSQLState() ? new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage())
                : new MySQLErrPacket(1, MySQLServerErrorCode.ER_INTERNAL_ERROR, cause.getMessage());
        }
        if (cause instanceof ShardingCTLException) {
            ShardingCTLException shardingCTLException = (ShardingCTLException) cause;
            return new MySQLErrPacket(1, ShardingCTLErrorCode.valueOf(shardingCTLException), shardingCTLException.getShardingCTL());
        }
        if (cause instanceof TableModifyInTransactionException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, ((TableModifyInTransactionException) cause).getTableName());
        }
        if (cause instanceof UnknownDatabaseException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_BAD_DB_ERROR, ((UnknownDatabaseException) cause).getDatabaseName());
        }
        if (cause instanceof NoDatabaseSelectedException) {
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
        if (cause instanceof ShardingTableRuleNotExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.SHARDING_TABLE_RULES_NOT_EXISTED, ((ShardingTableRuleNotExistedException) cause).getTableNames());
        }
        if (cause instanceof TablesInUsedException) {
            return new MySQLErrPacket(1, CommonErrorCode.TABLES_IN_USED, ((TablesInUsedException) cause).getTableNames());
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
        if (cause instanceof RuleNotExistsException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_SP_DOES_NOT_EXIST);
        }
        if (cause instanceof LockWaitTimeoutException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_LOCKING_SERVICE_TIMEOUT, ((LockWaitTimeoutException) cause).getTimeoutMilliseconds());
        }
        if (cause instanceof ResourceNotExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.RESOURCE_NOT_EXIST, ((ResourceNotExistedException) cause).getResourceNames());
        }
        if (cause instanceof ResourceInUsedException) {
            return new MySQLErrPacket(1, CommonErrorCode.RESOURCE_IN_USED, ((ResourceInUsedException) cause).getResourceNames());
        }
        if (cause instanceof ShardingRuleNotExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.SHARDING_RULE_NOT_EXIST);
        }
        if (cause instanceof ShardingTableRuleExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.SHARDING_TABLE_RULE_EXIST, ((ShardingTableRuleExistedException) cause).getTableNames());
        }
        if (cause instanceof ReplicaQueryRuleNotExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.REPLICA_QUERY_RULE_NOT_EXIST);
        }
        if (cause instanceof ReplicaQueryRuleDataSourcesNotExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.REPLICA_QUERY_RULE_DATA_SOURCE_NOT_EXIST, ((ReplicaQueryRuleDataSourcesNotExistedException) cause).getRuleNames());
        }
        if (cause instanceof AddReplicaQueryRuleDataSourcesExistedException) {
            return new MySQLErrPacket(1, CommonErrorCode.ADD_REPLICA_QUERY_RULE_DATA_SOURCE_EXIST, ((AddReplicaQueryRuleDataSourcesExistedException) cause).getRuleNames());
        }
        if (cause instanceof ReplicaQueryRuleCreateExistsException) {
            return new MySQLErrPacket(1, CommonErrorCode.REPLICA_QUERY_RULE_EXIST);
        }
        return new MySQLErrPacket(1, CommonErrorCode.UNKNOWN_EXCEPTION, cause.getMessage());
    }
}
