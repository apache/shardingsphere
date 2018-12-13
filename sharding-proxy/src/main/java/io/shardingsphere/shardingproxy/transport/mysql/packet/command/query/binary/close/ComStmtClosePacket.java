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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close;

import com.google.common.base.Optional;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * COM_STMT_CLOSE command packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-close.html">COM_QUERY</a>
 *
 * @author zhangyonglun
 */
@Slf4j
public final class ComStmtClosePacket implements CommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final int statementId;
    
    public ComStmtClosePacket(final int sequenceId, final MySQLPacketPayload payload) {
        this.sequenceId = sequenceId;
        statementId = payload.readInt4();
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_STMT_CLOSE received for Sharding-Proxy: {}", statementId);
        // TODO :yonglun need to clean PreparedStatementRegistry?
        return Optional.absent();
    }
}
