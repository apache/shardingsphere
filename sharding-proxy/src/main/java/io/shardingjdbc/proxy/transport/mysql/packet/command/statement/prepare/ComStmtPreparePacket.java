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

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement.prepare;

import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * COM_STMT_PREPARE command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-prepare.html">COM_STMT_PREPARE</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComStmtPreparePacket extends CommandPacket {
    
    private final String sql;
    
    public ComStmtPreparePacket(final MySQLPacketPayload mysqlPacketPayload) {
        sql = mysqlPacketPayload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeStringEOF(sql);
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        log.debug("COM_STMT_PREPARE received for Sharding-Proxy: {}", sql);
        List<DatabaseProtocolPacket> result = new LinkedList<>();
        // TODO write correct numColumns and numParams
        result.add(new ComStmtPrepareOKPacket(PreparedStatementRegistry.getInstance().register(sql), 0, 0, 0));
        // TODO add If num_params > 0
        // TODO add If numColumns > 0
        return result;
    }
}
