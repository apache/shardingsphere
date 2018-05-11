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

package io.shardingsphere.proxy.transport.mysql.packet.command.statement.prepare;

import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
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
    
    private final int numParameters;
    
    private final int warningCount;
    
    public ComStmtPrepareOKPacket(final int sequenceId, final int statementId, final int numColumns, final int numParameters, final int warningCount) {
        super(sequenceId);
        this.statementId = statementId;
        this.numColumns = numColumns;
        this.numParameters = numParameters;
        this.warningCount = warningCount;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(status);
        mysqlPacketPayload.writeInt4(statementId);
        // TODO Set numColumns=0 is a workaround to escape jdbc check for now, there's no issues found during a few tests.
        // TODO Column Definition Block should be added in future when the metadata of the columns is cached.
        mysqlPacketPayload.writeInt2(0);
        mysqlPacketPayload.writeInt2(numParameters);
        mysqlPacketPayload.writeReserved(1);
        mysqlPacketPayload.writeInt2(warningCount);
    }
}
