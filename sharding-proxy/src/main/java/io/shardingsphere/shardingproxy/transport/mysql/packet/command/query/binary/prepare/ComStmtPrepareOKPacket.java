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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare;

import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * COM_STMT_PREPARE_OK packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-prepare-response.html#packet-COM_STMT_PREPARE_OK">COM_STMT_PREPARE_OK</a>
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ComStmtPrepareOKPacket implements MySQLPacket {
    
    private static final int STATUS = 0x00;
    
    @Getter
    private final int sequenceId;
    
    private final int statementId;
    
    private final int columnsCount;
    
    private final int parametersCount;
    
    private final int warningCount;
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(STATUS);
        payload.writeInt4(statementId);
        // TODO Set columnsCount=0 is a workaround to escape jdbc check for now, there's no issues found during a few tests.
        // TODO Column Definition Block should be added in future when the metadata of the columns is cached.
        payload.writeInt2(0);
        payload.writeInt2(parametersCount);
        payload.writeReserved(1);
        payload.writeInt2(warningCount);
    }
}
