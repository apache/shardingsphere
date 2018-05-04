/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.packet.command.text.query;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.proxy.backend.common.SQLExecuteBackendHandler;
<<<<<<< HEAD
import io.shardingjdbc.proxy.backend.common.SQLPacketsBackendHandler;
import io.shardingjdbc.proxy.config.ShardingRuleRegistry;
=======
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
>>>>>>> upstream/dev
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandResponsePackets;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * COM_QUERY command packet.
 *
 * @author zhangliang
 * @author wangkai
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 */
@Slf4j
public final class ComQueryPacket extends CommandPacket {
    
    private final String sql;
    
<<<<<<< HEAD
    public ComQueryPacket(final int sequenceId, final int connectionId, MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId, connectionId);
=======
    private final SQLExecuteBackendHandler sqlExecuteBackendHandler;
    
    public ComQueryPacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
>>>>>>> upstream/dev
        sql = mysqlPacketPayload.readStringEOF();
        sqlExecuteBackendHandler = new SQLExecuteBackendHandler(sql, DatabaseType.MySQL, true);
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeStringEOF(sql);
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
<<<<<<< HEAD
        if (ShardingRuleRegistry.WITHOUT_JDBC) {
            return new SQLPacketsBackendHandler(sql, connectionId, DatabaseType.MySQL, true).execute();
        } else {
            return new SQLExecuteBackendHandler(sql, DatabaseType.MySQL, true).execute();
        }
=======
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
>>>>>>> upstream/dev
    }
}
