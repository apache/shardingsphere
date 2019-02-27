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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.exception.BackendException;
import org.apache.shardingsphere.shardingproxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.shardingproxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.shardingproxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.ShardingCTLErrorCode;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.ShardingCTLException;
import org.apache.shardingsphere.shardingproxy.error.CommonErrorCode;
import org.apache.shardingsphere.shardingproxy.error.SQLErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;

import java.sql.SQLException;

/**
 * MySQL ERR packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html">ERR Packet</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@RequiredArgsConstructor
@Getter
public final class MySQLErrPacket implements MySQLPacket {
    
    /**
     * Header of ERR packet.
     */
    public static final int HEADER = 0xff;
    
    private static final String SQL_STATE_MARKER = "#";
    
    private final int sequenceId;
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public MySQLErrPacket(final int sequenceId, final SQLErrorCode sqlErrorCode, final Object... errorMessageArguments) {
        this(sequenceId, sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), errorMessageArguments));
    }
    
    public MySQLErrPacket(final int sequenceId, final ShardingCTLException cause) {
        this(sequenceId, ShardingCTLErrorCode.valueOf(cause), cause.getShardingCTL());
    }
    
    public MySQLErrPacket(final int sequenceId, final SQLException cause) {
        this(sequenceId, cause.getErrorCode(), cause.getSQLState(), cause.getMessage());
    }
    
    public MySQLErrPacket(final int sequenceId, final BackendException cause) {
        this(sequenceId, getErrorCode(cause), getSQLState(cause), getErrorMessage(cause));
    }
    
    private static int getErrorCode(final BackendException cause) {
        if (cause instanceof TableModifyInTransactionException) {
            return MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getErrorCode();
        }
        if (cause instanceof UnknownDatabaseException) {
            return MySQLServerErrorCode.ER_BAD_DB_ERROR.getErrorCode();
        }
        if (cause instanceof NoDatabaseSelectedException) {
            return MySQLServerErrorCode.ER_NO_DB_ERROR.getErrorCode();
        }
        return CommonErrorCode.UNKNOWN_EXCEPTION.getErrorCode();
    }
    
    private static String getSQLState(final BackendException cause) {
        if (cause instanceof TableModifyInTransactionException) {
            return MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getSqlState();
        }
        if (cause instanceof UnknownDatabaseException) {
            return MySQLServerErrorCode.ER_BAD_DB_ERROR.getSqlState();
        }
        if (cause instanceof NoDatabaseSelectedException) {
            return MySQLServerErrorCode.ER_NO_DB_ERROR.getSqlState();
        }
        return CommonErrorCode.UNKNOWN_EXCEPTION.getSqlState();
    }
    
    private static String getErrorMessage(final BackendException cause) {
        if (cause instanceof TableModifyInTransactionException) {
            return String.format(MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getErrorMessage(), ((TableModifyInTransactionException) cause).getTableName());
        }
        if (cause instanceof UnknownDatabaseException) {
            return String.format(MySQLServerErrorCode.ER_BAD_DB_ERROR.getErrorMessage(), ((UnknownDatabaseException) cause).getDatabaseName());
        }
        if (cause instanceof NoDatabaseSelectedException) {
            return MySQLServerErrorCode.ER_NO_DB_ERROR.getErrorMessage();
        }
        return String.format(CommonErrorCode.UNKNOWN_EXCEPTION.getErrorMessage(), cause.getMessage());
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeInt2(errorCode);
        payload.writeStringFix(SQL_STATE_MARKER);
        payload.writeStringFix(sqlState);
        payload.writeStringEOF(errorMessage);
    }
}
