/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command.text.query;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.proxy.backend.common.SQLExecuteBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transaction.AtomikosUserTransaction;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * COM_QUERY command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComQueryPacket extends CommandPacket {
    
    private final String sql;
    
    private final SQLExecuteBackendHandler sqlExecuteBackendHandler;
    
    public ComQueryPacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        sql = mysqlPacketPayload.readStringEOF();
        sqlExecuteBackendHandler = new SQLExecuteBackendHandler(sql, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeStringEOF(sql);
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
        try {
            if (doTransactionIntercept()) {
                return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            }
        } catch (final Exception ex) {
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "", "" + ex.getMessage()));
        }
        return sqlExecuteBackendHandler.execute();
    }
    
    /**
     * Has more Result value.
     *
     * @return has more result value
     */
    public boolean hasMoreResultValue() {
        try {
            return sqlExecuteBackendHandler.hasMoreResultValue();
        } catch (final SQLException ex) {
            return false;
        }
    }
    
    /**
     * Get result value.
     *
     * @return database protocol packet
     */
    public DatabaseProtocolPacket getResultValue() {
        return sqlExecuteBackendHandler.getResultValue();
    }
    
    private boolean doTransactionIntercept() throws Exception {
        if (RuleRegistry.isXaTransaction()) {
            if (isXaBegin()) {
                AtomikosUserTransaction.getInstance().begin();
                return true;
            } else if (isXaCommit()) {
                AtomikosUserTransaction.getInstance().commit();
                return true;
            } else if (isXaRollback()) {
                AtomikosUserTransaction.getInstance().rollback();
                return true;
            }
        }
        return false;
    }
    
    private boolean isXaBegin() {
        return "BEGIN".equalsIgnoreCase(sql) || "START TRANSACTION".equalsIgnoreCase(sql) || "SET AUTOCOMMIT=0".equalsIgnoreCase(sql);
    }
    
    private boolean isXaCommit() {
        return "COMMIT".equalsIgnoreCase(sql);
    }
    
    private boolean isXaRollback() {
        return "ROLLBACK".equalsIgnoreCase(sql);
    }
}
