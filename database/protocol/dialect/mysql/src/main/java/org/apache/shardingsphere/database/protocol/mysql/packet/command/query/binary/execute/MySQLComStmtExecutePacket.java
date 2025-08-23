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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * COM_STMT_EXECUTE command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_execute.html">COM_STMT_EXECUTE</a>
 */
@Getter
public final class MySQLComStmtExecutePacket extends MySQLCommandPacket {
    
    private static final int ITERATION_COUNT = 1;
    
    private static final int NULL_BITMAP_OFFSET = 0;
    
    private final MySQLPacketPayload payload;
    
    private final int statementId;
    
    private final int flags;
    
    @Getter(AccessLevel.NONE)
    private final MySQLNullBitmap nullBitmap;
    
    private final MySQLNewParametersBoundFlag newParametersBoundFlag;
    
    private final List<MySQLPreparedStatementParameterType> newParameterTypes;
    
    public MySQLComStmtExecutePacket(final MySQLPacketPayload payload, final int paramCount) {
        super(MySQLCommandPacketType.COM_STMT_EXECUTE);
        this.payload = payload;
        statementId = payload.readInt4();
        flags = payload.readInt1();
        Preconditions.checkArgument(ITERATION_COUNT == payload.readInt4());
        if (paramCount > 0) {
            nullBitmap = new MySQLNullBitmap(paramCount, NULL_BITMAP_OFFSET);
            for (int i = 0; i < nullBitmap.getNullBitmap().length; i++) {
                nullBitmap.getNullBitmap()[i] = payload.readInt1();
            }
            newParametersBoundFlag = MySQLNewParametersBoundFlag.valueOf(payload.readInt1());
            newParameterTypes = MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST == newParametersBoundFlag ? getNewParameterTypes(paramCount) : Collections.emptyList();
        } else {
            nullBitmap = null;
            newParametersBoundFlag = null;
            newParameterTypes = Collections.emptyList();
        }
    }
    
    private List<MySQLPreparedStatementParameterType> getNewParameterTypes(final int paramCount) {
        List<MySQLPreparedStatementParameterType> result = new ArrayList<>(paramCount);
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            MySQLBinaryColumnType columnType = MySQLBinaryColumnType.valueOf(payload.readInt1());
            int unsignedFlag = payload.readInt1();
            result.add(new MySQLPreparedStatementParameterType(columnType, unsignedFlag));
        }
        return result;
    }
    
    /**
     * Read parameter values from packet.
     *
     * @param paramTypes parameter type of values
     * @param longDataIndexes indexes of long data
     * @param parameterFlags column definition flag of parameters
     * @return parameter values
     * @throws SQLException SQL exception
     */
    public List<Object> readParameters(final List<MySQLPreparedStatementParameterType> paramTypes, final Set<Integer> longDataIndexes,
                                       final List<Integer> parameterFlags) throws SQLException {
        List<Object> result = new ArrayList<>(paramTypes.size());
        for (int paramIndex = 0; paramIndex < paramTypes.size(); paramIndex++) {
            if (longDataIndexes.contains(paramIndex)) {
                result.add(null);
                continue;
            }
            MySQLBinaryProtocolValue binaryProtocolValue = MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(paramTypes.get(paramIndex).getColumnType());
            Object value = nullBitmap.isNullParameter(paramIndex) ? null
                    : binaryProtocolValue.read(payload, (parameterFlags.get(paramIndex) & MySQLColumnDefinitionFlag.UNSIGNED.getValue()) == MySQLColumnDefinitionFlag.UNSIGNED.getValue());
            result.add(value);
        }
        return result;
    }
}
