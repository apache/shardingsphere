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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;

/**
 * COM_STMT_CLOSE command packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-close.html">COM_QUERY</a>
 *
 * @author zhangyonglun
 */
@Slf4j
public final class MySQLComStmtClosePacket implements MySQLCommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final int statementId;
    
    public MySQLComStmtClosePacket(final int sequenceId, final MySQLPacketPayload payload) {
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
