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
import io.shardingsphere.core.constant.TCLType;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.transaction.event.XaTransactionEvent;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.proxy.backend.common.SQLExecuteBackendHandler;
import io.shardingsphere.proxy.backend.common.SQLPacketsBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transaction.AtomikosUserTransaction;
import io.shardingsphere.proxy.transport.common.packet.CommandPacketRebuilder;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.SQLException;

/**
 * COM_QUERY command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 *
 * @author zhangliang
 * @author linjiaqi
 * @author zhaojun
 */
@Slf4j
public final class ComQueryPacket extends CommandPacket implements CommandPacketRebuilder {
    
    private final int connectionId;
    
    private final String sql;
    
    private final SQLExecuteBackendHandler sqlExecuteBackendHandler;
    
    private final SQLPacketsBackendHandler sqlPacketsBackendHandler;
    
    public ComQueryPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        this.connectionId = connectionId;
        sql = mysqlPacketPayload.readStringEOF();
        if (RuleRegistry.getInstance().isWithoutJdbc()) {
            sqlExecuteBackendHandler = null;
            sqlPacketsBackendHandler = new SQLPacketsBackendHandler(this, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
        } else {
            sqlPacketsBackendHandler = null;
            sqlExecuteBackendHandler = new SQLExecuteBackendHandler(sql, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
        }
    }
    
    public ComQueryPacket(final int sequenceId, final int connectionId, final String sql) {
        super(sequenceId);
        this.connectionId = connectionId;
        this.sql = sql;
        sqlExecuteBackendHandler = null;
        sqlPacketsBackendHandler = null;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(CommandPacketType.COM_QUERY.getValue());
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
            log.error("doTransactionIntercept Exception", ex);
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "" + ex.getMessage()));
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
            return RuleRegistry.getInstance().isWithoutJdbc() ? sqlPacketsBackendHandler.hasMoreResultValue() : sqlExecuteBackendHandler.hasMoreResultValue();
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
        return RuleRegistry.getInstance().isWithoutJdbc() ? sqlPacketsBackendHandler.getResultValue() : sqlExecuteBackendHandler.getResultValue();
    }
    
    @Override
    public int connectionId() {
        return connectionId;
    }
    
    @Override
    public int sequenceId() {
        return getSequenceId();
    }
    
    @Override
    public String sql() {
        return sql;
    }
    
    @Override
    public CommandPacket rebuild(final Object... params) {
        return new ComQueryPacket((int) params[0], (int) params[1], (String) params[2]);
    }
    
    private boolean doTransactionIntercept() throws Exception {
        boolean result = false;
        if (TransactionType.XA.equals(RuleRegistry.getInstance().getTransactionType())) {
            XaTransactionEvent xaTransactionEvent = new XaTransactionEvent(sql);
            if (isBegin()) {
                xaTransactionEvent.setTclType(TCLType.BEGIN);
                result = true;
            } else if (isCommit()) {
                xaTransactionEvent.setTclType(TCLType.COMMIT);
                result = true;
            } else if (isXaRollback()) {
                xaTransactionEvent.setTclType(TCLType.ROLLBACK);
                result = true;
            }
            if (result) {
                EventBusInstance.getInstance().post(xaTransactionEvent);
            }
        } else {
            if (isBegin() || isCommit() || isRollback()) {
                result = true;
            }
        }
        return result;
    }
    
    private boolean isBegin() {
        return "BEGIN".equalsIgnoreCase(sql) || "START TRANSACTION".equalsIgnoreCase(sql) || "SET AUTOCOMMIT=0".equalsIgnoreCase(sql);
    }
    
    private boolean isCommit() {
        return "COMMIT".equalsIgnoreCase(sql);
    }
    
    private boolean isXaRollback() throws SystemException {
        return "ROLLBACK".equalsIgnoreCase(sql) && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    private boolean isRollback() {
        return "ROLLBACK".equalsIgnoreCase(sql);
    }
}
