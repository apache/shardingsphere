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

import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * COM_STMT_PREPARE_OK packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-prepare-response.html#packet-COM_STMT_PREPARE_OK">COM_STMT_PREPARE_OK</a>
 *
 * @author zhangliang
 */
@Getter
public final class ComStmtPrepareOKPacket extends MySQLPacket {
    
    private final int status = 0x00;
    
    private final int statementId;
    
    private final int numColumns;
    
    private final int numParams;
    
    private final int warningCount;
    
    public ComStmtPrepareOKPacket(final int sequenceId, final int statementId, final int numColumns, final int numParams, final int warningCount) {
        super(sequenceId);
        this.statementId = statementId;
        this.numColumns = numColumns;
        this.numParams = numParams;
        this.warningCount = warningCount;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(status);
        mysqlPacketPayload.writeInt4(statementId);
        mysqlPacketPayload.writeInt2(numColumns);
        mysqlPacketPayload.writeInt2(numParams);
        mysqlPacketPayload.writeReserved(1);
        mysqlPacketPayload.writeInt2(warningCount);
    }
}
