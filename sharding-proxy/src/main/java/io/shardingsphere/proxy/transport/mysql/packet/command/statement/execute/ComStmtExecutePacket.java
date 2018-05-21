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

package io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.proxy.backend.common.StatementExecuteBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.NewParametersBoundFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * COM_STMT_EXECUTE command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-execute.html">COM_QUERY</a>
 *
 * @author zhangyonglun
 */
@Getter
@Slf4j
public final class ComStmtExecutePacket extends CommandPacket {
    
    private static final ColumnType NULL_PARAMETER_DEFAULT_COLUMN_TYPE = ColumnType.MYSQL_TYPE_STRING;
    
    private static final int NULL_PARAMETER_DEFAULT_UNSIGNED_FLAG = 0;
    
    private static final int RESERVED_BIT_LENGTH = 0;
    
    private final int statementId;
    
    private final int flags;
    
    private final int iterationCount = 1;
    
    private final NullBitmap nullBitmap;
    
    private final NewParametersBoundFlag newParametersBoundFlag;
    
    private final List<PreparedStatementParameter> preparedStatementParameters = new ArrayList<>(32);
    
    private final StatementExecuteBackendHandler statementExecuteBackendHandler;
    
    public ComStmtExecutePacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        statementId = mysqlPacketPayload.readInt4();
        flags = mysqlPacketPayload.readInt1();
        Preconditions.checkArgument(iterationCount == mysqlPacketPayload.readInt4());
        SQLStatement sqlStatement = new SQLParsingEngine(DatabaseType.MySQL, PreparedStatementRegistry.getInstance().getSQL(statementId),
            RuleRegistry.getInstance().getShardingRule(), null).parse(true);
        int numParameters = sqlStatement.getParametersIndex();
        nullBitmap = new NullBitmap(numParameters, RESERVED_BIT_LENGTH);
        for (int i = 0; i < nullBitmap.getNullBitmap().length; i++) {
            nullBitmap.getNullBitmap()[i] = mysqlPacketPayload.readInt1();
        }
        newParametersBoundFlag = NewParametersBoundFlag.valueOf(mysqlPacketPayload.readInt1());
        setParameterList(mysqlPacketPayload, numParameters, newParametersBoundFlag);
        statementExecuteBackendHandler = new StatementExecuteBackendHandler(preparedStatementParameters, statementId, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
    }
    
    private void setParameterList(final MySQLPacketPayload mysqlPacketPayload, final int numParameters, final NewParametersBoundFlag newParametersBoundFlag) {
        if (NewParametersBoundFlag.PARAMETER_TYPE_EXIST == newParametersBoundFlag) {
            setParameterHeader(mysqlPacketPayload, numParameters);
        } else if (NewParametersBoundFlag.PARAMETER_TYPE_NOT_EXIST == newParametersBoundFlag) {
            setParameterHeaderFromCache(numParameters);
        }
        setParameterValue(mysqlPacketPayload, numParameters);
    }
    
    private void setParameterHeader(final MySQLPacketPayload mysqlPacketPayload, final int numParameters) {
        List<PreparedStatementParameterHeader> parameterHeaders = new ArrayList<>(32);
        for (int i = 0; i < numParameters; i++) {
            if (nullBitmap.isParameterNull(i)) {
                preparedStatementParameters.add(new PreparedStatementParameter(NULL_PARAMETER_DEFAULT_COLUMN_TYPE, NULL_PARAMETER_DEFAULT_UNSIGNED_FLAG, null));
                continue;
            }
            ColumnType columnType = ColumnType.valueOf(mysqlPacketPayload.readInt1());
            int unsignedFlag = mysqlPacketPayload.readInt1();
            preparedStatementParameters.add(new PreparedStatementParameter(columnType, unsignedFlag));
            parameterHeaders.add(new PreparedStatementParameterHeader(columnType, unsignedFlag));
        }
        PreparedStatementRegistry.getInstance().setParameterHeaders(statementId, parameterHeaders);
    }
    
    private void setParameterHeaderFromCache(final int numParameters) {
        for (int i = 0; i < numParameters; i++) {
            if (nullBitmap.isParameterNull(i)) {
                preparedStatementParameters.add(new PreparedStatementParameter(NULL_PARAMETER_DEFAULT_COLUMN_TYPE, NULL_PARAMETER_DEFAULT_UNSIGNED_FLAG, null));
                continue;
            }
            PreparedStatementParameterHeader preparedStatementParameterHeader = PreparedStatementRegistry.getInstance().getParameterHeader(statementId);
            preparedStatementParameters.add(new PreparedStatementParameter(preparedStatementParameterHeader.getColumnType(), preparedStatementParameterHeader.getUnsignedFlag()));
        }
    }
    
    private void setParameterValue(final MySQLPacketPayload mysqlPacketPayload, final int numParameters) {
        for (int i = 0; i < numParameters; i++) {
            if (nullBitmap.isParameterNull(i)) {
                continue;
            }
            PreparedStatementParameter preparedStatementParameter = preparedStatementParameters.get(i);
            ColumnType columnType = preparedStatementParameter.getColumnType();
            preparedStatementParameter.setValue(BinaryProtocolValueUtility.getInstance().readBinaryProtocolValue(columnType, mysqlPacketPayload));
        }
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt4(statementId);
        mysqlPacketPayload.writeInt1(flags);
        mysqlPacketPayload.writeInt4(iterationCount);
        for (int each : nullBitmap.getNullBitmap()) {
            mysqlPacketPayload.writeInt1(each);
        }
        mysqlPacketPayload.writeInt1(newParametersBoundFlag.getValue());
        for (PreparedStatementParameter each : preparedStatementParameters) {
            mysqlPacketPayload.writeInt1(each.getColumnType().getValue());
            mysqlPacketPayload.writeInt1(each.getUnsignedFlag());
            mysqlPacketPayload.writeStringLenenc((String) each.getValue());
        }
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("COM_STMT_EXECUTE received for Sharding-Proxy: {}", statementId);
        return statementExecuteBackendHandler.execute();
    }
    
    /**
     * Has more Result value.
     *
     * @return has more result value
     */
    public boolean hasMoreResultValue() {
        try {
            return statementExecuteBackendHandler.hasMoreResultValue();
        } catch (final SQLException ex) {
            return false;
        }
    }
    
    /**
     * Get result value.
     *
     * @return database protocol packet
     */
    public DatabaseProtocolPacket getResultValue() {
        return statementExecuteBackendHandler.getResultValue();
    }
}
