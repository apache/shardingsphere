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

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement.execute;

import com.google.common.base.Preconditions;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * COM_STMT_EXECUTE command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-execute.html">COM_QUERY</a>
 *
 * @author zhangyonglun
 */
@Slf4j
@Getter
public final class ComStmtExecutePacket extends CommandPacket {
    
    private final int statementId;
    
    private final int flags;
    
    private final int iterationCount = 1;
    
    private final int[] nullBitmap;
    
    private final int newParamsBoundFlag;
    
    private final List<Parameter> parameterList = new ArrayList<>();
    
    public ComStmtExecutePacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        statementId = mysqlPacketPayload.readInt4();
        flags = mysqlPacketPayload.readInt1();
        Preconditions.checkArgument(iterationCount == mysqlPacketPayload.readInt4());
        int numColumns = PreparedStatementRegistry.getInstance().getNumColumns(statementId);
        nullBitmap = new int[(numColumns + 7) / 8];
        for (int i = 0; i < nullBitmap.length; i++) {
            nullBitmap[i] = mysqlPacketPayload.readInt1();
        }
        newParamsBoundFlag = mysqlPacketPayload.readInt1();
        while (mysqlPacketPayload.isReadable()) {
            parameterList.add(new Parameter(mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1(), mysqlPacketPayload.readStringLenenc()));
        }
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt4(statementId);
        mysqlPacketPayload.writeInt1(flags);
        mysqlPacketPayload.writeInt4(iterationCount);
        for (int each : nullBitmap) {
            mysqlPacketPayload.writeInt1(each);
        }
        mysqlPacketPayload.writeInt1(newParamsBoundFlag);
        for (Parameter each : parameterList) {
            mysqlPacketPayload.writeInt1(each.getColumnType().getValue());
            mysqlPacketPayload.writeInt1(each.getUnsignedFlag());
            mysqlPacketPayload.writeStringLenenc(each.getValue());
        }
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        log.debug("COM_STMT_EXECUTE received for Sharding-Proxy: {}", statementId);
        return null;
    }
}
