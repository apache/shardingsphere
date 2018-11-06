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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command;

import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.UnsupportedCommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.ComInitDbPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.ping.ComPingPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit.ComQuitPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.ComStmtClosePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.ComStmtExecutePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.ComStmtPreparePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.ComFieldListPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.ComQueryPacket;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.SQLException;

/**
 * Command packet factory.
 *
 * @author zhangliang
 * @author wangkai
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandPacketFactory {
    
    /**
     * Create new instance of command packet.
     *
     * @param sequenceId        sequence id
     * @param connectionId      MySQL connection id
     * @param payload           MySQL packet payload
     * @param backendConnection backend connection
     * @param frontendHandler   frontend handler
     * @return command packet
     * @throws SQLException SQL exception
     */
    public static CommandPacket newInstance(final int sequenceId, final int connectionId, final MySQLPacketPayload payload, 
                                            final BackendConnection backendConnection, final FrontendHandler frontendHandler) throws SQLException {
        int commandPacketTypeValue = payload.readInt1();
        CommandPacketType type = CommandPacketType.valueOf(commandPacketTypeValue);
        switch (type) {
            case COM_QUIT:
                return new ComQuitPacket(sequenceId);
            case COM_INIT_DB:
                return new ComInitDbPacket(sequenceId, payload, frontendHandler);
            case COM_FIELD_LIST:
                return new ComFieldListPacket(sequenceId, connectionId, frontendHandler.getCurrentSchema(), payload, backendConnection);
            case COM_QUERY:
                return new ComQueryPacket(sequenceId, connectionId, payload, backendConnection, frontendHandler);
            case COM_STMT_PREPARE:
                return new ComStmtPreparePacket(sequenceId, frontendHandler.getCurrentSchema(), payload);
            case COM_STMT_EXECUTE:
                return new ComStmtExecutePacket(sequenceId, connectionId, frontendHandler.getCurrentSchema(), payload, backendConnection);
            case COM_STMT_CLOSE:
                return new ComStmtClosePacket(sequenceId, payload);
            case COM_PING:
                return new ComPingPacket(sequenceId);
            default:
                return new UnsupportedCommandPacket(sequenceId, type);
        }
    }
}
