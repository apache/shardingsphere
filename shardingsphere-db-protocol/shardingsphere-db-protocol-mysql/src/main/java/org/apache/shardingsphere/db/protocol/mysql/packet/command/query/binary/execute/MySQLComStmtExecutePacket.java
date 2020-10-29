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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatement;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatementParameterType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * COM_STMT_EXECUTE command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-execute.html">COM_STMT_EXECUTE</a>
 */
@ToString(of = {"sql", "parameters"})
public final class MySQLComStmtExecutePacket extends MySQLCommandPacket {
    
    private static final int ITERATION_COUNT = 1;
    
    private static final int NULL_BITMAP_OFFSET = 0;
    
    private final int statementId;
    
    private final MySQLBinaryStatement binaryStatement;
    
    private final int flags;
    
    private final MySQLNullBitmap nullBitmap;
    
    private final MySQLNewParametersBoundFlag newParametersBoundFlag;
    
    @Getter
    private final String sql;
    
    @Getter
    private final List<Object> parameters;
    
    public MySQLComStmtExecutePacket(final MySQLPacketPayload payload) throws SQLException {
        super(MySQLCommandPacketType.COM_STMT_EXECUTE);
        statementId = payload.readInt4();
        binaryStatement = MySQLBinaryStatementRegistry.getInstance().getBinaryStatement(statementId);
        flags = payload.readInt1();
        Preconditions.checkArgument(ITERATION_COUNT == payload.readInt4());
        int parameterCount = binaryStatement.getParameterCount();
        sql = binaryStatement.getSql();
        if (parameterCount > 0) {
            nullBitmap = new MySQLNullBitmap(parameterCount, NULL_BITMAP_OFFSET);
            for (int i = 0; i < nullBitmap.getNullBitmap().length; i++) {
                nullBitmap.getNullBitmap()[i] = payload.readInt1();
            }
            newParametersBoundFlag = MySQLNewParametersBoundFlag.valueOf(payload.readInt1());
            if (MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST == newParametersBoundFlag) {
                binaryStatement.setParameterTypes(getParameterTypes(payload, parameterCount));
            }
            parameters = getParameters(payload, parameterCount);
        } else {
            nullBitmap = null;
            newParametersBoundFlag = null;
            parameters = Collections.emptyList();
        }
    }
    
    private List<MySQLBinaryStatementParameterType> getParameterTypes(final MySQLPacketPayload payload, final int parameterCount) {
        List<MySQLBinaryStatementParameterType> result = new ArrayList<>(parameterCount);
        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
            MySQLColumnType columnType = MySQLColumnType.valueOf(payload.readInt1());
            int unsignedFlag = payload.readInt1();
            result.add(new MySQLBinaryStatementParameterType(columnType, unsignedFlag));
        }
        return result;
    }
    
    private List<Object> getParameters(final MySQLPacketPayload payload, final int parameterCount) throws SQLException {
        List<Object> result = new ArrayList<>(parameterCount);
        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
            MySQLBinaryProtocolValue binaryProtocolValue = MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(binaryStatement.getParameterTypes().get(parameterIndex).getColumnType());
            result.add(nullBitmap.isNullParameter(parameterIndex) ? null : binaryProtocolValue.read(payload));
        }
        return result;
    }
    
    @Override
    public void doWrite(final MySQLPacketPayload payload) {
        payload.writeInt4(statementId);
        payload.writeInt1(flags);
        payload.writeInt4(ITERATION_COUNT);
        if (binaryStatement.getParameterCount() > 0) {
            for (int each : nullBitmap.getNullBitmap()) {
                payload.writeInt1(each);
            }
            payload.writeInt1(newParametersBoundFlag.getValue());
            int count = 0;
            for (Object each : parameters) {
                MySQLBinaryStatementParameterType parameterType = binaryStatement.getParameterTypes().get(count);
                payload.writeInt1(parameterType.getColumnType().getValue());
                payload.writeInt1(parameterType.getUnsignedFlag());
                payload.writeStringLenenc(null == each ? "" : each.toString());
                count++;
            }
        }
    }
}
