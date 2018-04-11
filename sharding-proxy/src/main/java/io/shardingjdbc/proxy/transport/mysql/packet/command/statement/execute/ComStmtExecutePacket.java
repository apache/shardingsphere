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
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.proxy.backend.common.StatementExecuteBackendHandler;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
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
        int numParams = PreparedStatementRegistry.getInstance().getNumParams(statementId);
        nullBitmap = new int[(numParams + 7) / 8];
        for (int i = 0; i < nullBitmap.length; i++) {
            nullBitmap[i] = mysqlPacketPayload.readInt1();
        }
        newParamsBoundFlag = mysqlPacketPayload.readInt1();
        setParameterList(mysqlPacketPayload, numParams);
    }
    
    private void setParameterList(final MySQLPacketPayload mysqlPacketPayload, final int numParams) {
        List<Integer> params = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; i++) {
            params.add(mysqlPacketPayload.readInt1());
            params.add(mysqlPacketPayload.readInt1());
        }
        for (int i = 0; i < numParams; i++) {
            ColumnType columnType = ColumnType.valueOf(params.get(i * 2));
            int unsignedFlag = params.get(i * 2 + 1);
    
            // TODO add more types
            if (columnType == ColumnType.MYSQL_TYPE_LONG) {
                int value = mysqlPacketPayload.readInt4();
                parameterList.add(new Parameter(columnType, unsignedFlag, String.valueOf(value)));
            } else {
                parameterList.add(new Parameter(columnType, unsignedFlag, mysqlPacketPayload.readStringLenenc()));
            }
        }
    }
    
    /**
     * Is parameter null.
     *
     * @param index column index
     * @return is parameter null
     */
    public boolean isParameterNull(final int index) {
        int bytePos = index / 8;
        int bitPos = index % 8;
        return (nullBitmap[bytePos] & (1 << bitPos)) != 0;
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
            mysqlPacketPayload.writeStringLenenc((String) each.getValue());
        }
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        log.debug("COM_STMT_EXECUTE received for Sharding-Proxy: {}", statementId);
        return new StatementExecuteBackendHandler(this, DatabaseType.MySQL, true).execute();
    }
}
