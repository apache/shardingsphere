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
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.proxy.backend.common.StatementExecuteBackendHandler;
import io.shardingjdbc.proxy.config.ShardingRuleRegistry;
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
    
    private final int newParametersBoundFlag;
    
    private final List<PreparedStatementParameter> preparedStatementParameters = new ArrayList<>();
    
    public ComStmtExecutePacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        statementId = mysqlPacketPayload.readInt4();
        flags = mysqlPacketPayload.readInt1();
        Preconditions.checkArgument(iterationCount == mysqlPacketPayload.readInt4());
        SQLStatement sqlStatement = new SQLParsingEngine(DatabaseType.MySQL, PreparedStatementRegistry.getInstance().getSql(statementId),
            ShardingRuleRegistry.getInstance().getShardingRule()).parse(true);
        int numParameters = sqlStatement.getParametersIndex();
        nullBitmap = new int[(numParameters + 7) / 8];
        for (int i = 0; i < nullBitmap.length; i++) {
            nullBitmap[i] = mysqlPacketPayload.readInt1();
        }
        newParametersBoundFlag = mysqlPacketPayload.readInt1();
        setParameterList(mysqlPacketPayload, numParameters);
    }
    
    private void setParameterList(final MySQLPacketPayload mysqlPacketPayload, final int numParameters) {
        for (int i = 0; i < numParameters; i++) {
            ColumnType columnType = ColumnType.valueOf(mysqlPacketPayload.readInt1());
            int unsignedFlag = mysqlPacketPayload.readInt1();
            preparedStatementParameters.add(new PreparedStatementParameter(columnType, unsignedFlag, ""));
        }
        for (int i = 0; i < numParameters; i++) {
            PreparedStatementParameter preparedStatementParameter = preparedStatementParameters.get(i);
            // TODO add more types
            if (preparedStatementParameter.getColumnType() == ColumnType.MYSQL_TYPE_LONG) {
                preparedStatementParameter.setValue(String.valueOf(mysqlPacketPayload.readInt4()));
            } else {
                preparedStatementParameter.setValue(String.valueOf(mysqlPacketPayload.readStringLenenc()));
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
        int bytePosition = index / 8;
        int bitPosition = index % 8;
        return (nullBitmap[bytePosition] & (1 << bitPosition)) != 0;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt4(statementId);
        mysqlPacketPayload.writeInt1(flags);
        mysqlPacketPayload.writeInt4(iterationCount);
        for (int each : nullBitmap) {
            mysqlPacketPayload.writeInt1(each);
        }
        mysqlPacketPayload.writeInt1(newParametersBoundFlag);
        for (PreparedStatementParameter each : preparedStatementParameters) {
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
