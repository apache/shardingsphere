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

import java.sql.SQLException;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.proxy.backend.common.SQLExecuteBackendHandler;
import io.shardingsphere.proxy.backend.common.SQLPacketsBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transaction.AtomikosUserTransaction;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import lombok.extern.slf4j.Slf4j;

/**
 * COM_QUERY command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 *
 * @author zhangliang
 * @author linjiaqi
 */
@Slf4j
public final class ComQueryPacket extends CommandPacket implements Cloneable {
    
    private final SQLExecuteBackendHandler sqlExecuteBackendHandler;
    
    private final SQLPacketsBackendHandler sqlPacketsBackendHandler;

    public ComQueryPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId, connectionId);
        setSql(mysqlPacketPayload.readStringEOF());
        sqlExecuteBackendHandler = new SQLExecuteBackendHandler(getSql(), DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
        sqlPacketsBackendHandler = new SQLPacketsBackendHandler(this, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(CommandPacketType.COM_QUERY.getValue());
        mysqlPacketPayload.writeStringEOF(getSql());
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
        try {
            doTransactionIntercept();
        } catch (final Exception ex) {
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "", "" + ex.getMessage()));
        }
        if (RuleRegistry.getInstance().isWithoutJdbc()) {
            return sqlPacketsBackendHandler.execute();
        } else {
            return sqlExecuteBackendHandler.execute();
        }
    }
    
    /**
     * Has more Result value.
     *
     * @return has more result value
     */
    public boolean hasMoreResultValue() {
        try {
            if (RuleRegistry.getInstance().isWithoutJdbc()) {
                return sqlPacketsBackendHandler.hasMoreResultValue();
            } else {
                return sqlExecuteBackendHandler.hasMoreResultValue();
            }
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
        if (RuleRegistry.getInstance().isWithoutJdbc()) {
            return sqlPacketsBackendHandler.getResultValue();
        } else {
            return sqlExecuteBackendHandler.getResultValue();
        }
    }

    private void doTransactionIntercept() throws Exception {
        if (RuleRegistry.isXaTransaction()) {
            if (isXaBegin()) {
                AtomikosUserTransaction.getInstance().begin();
            } else if (isXaCommit()) {
                AtomikosUserTransaction.getInstance().commit();
            } else if (isXaRollback()) {
                AtomikosUserTransaction.getInstance().rollback();
            }
        }
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
