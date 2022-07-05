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
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * COM_STMT_EXECUTE command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-execute.html">COM_STMT_EXECUTE</a>
 */
@ToString(of = {"statementId"})
public final class MySQLComStmtExecutePacket extends MySQLCommandPacket {
    
    private static final int ITERATION_COUNT = 1;
    
    private static final int NULL_BITMAP_OFFSET = 0;
    
    private final MySQLPacketPayload payload;
    
    @Getter
    private final int statementId;
    
    private final int flags;
    
    private final MySQLNullBitmap nullBitmap;
    
    @Getter
    private final MySQLNewParametersBoundFlag newParametersBoundFlag;
    
    @Getter
    private final List<MySQLPreparedStatementParameterType> newParameterTypes;
    
    public MySQLComStmtExecutePacket(final MySQLPacketPayload payload, final int parameterCount) throws SQLException {
        super(MySQLCommandPacketType.COM_STMT_EXECUTE);
        this.payload = payload;
        statementId = payload.readInt4();
        flags = payload.readInt1();
        Preconditions.checkArgument(ITERATION_COUNT == payload.readInt4());
        if (parameterCount > 0) {
            nullBitmap = new MySQLNullBitmap(parameterCount, NULL_BITMAP_OFFSET);
            for (int i = 0; i < nullBitmap.getNullBitmap().length; i++) {
                nullBitmap.getNullBitmap()[i] = payload.readInt1();
            }
            newParametersBoundFlag = MySQLNewParametersBoundFlag.valueOf(payload.readInt1());
            newParameterTypes = MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST == newParametersBoundFlag ? getNewParameterTypes(parameterCount) : Collections.emptyList();
        } else {
            nullBitmap = null;
            newParametersBoundFlag = null;
            newParameterTypes = Collections.emptyList();
        }
    }
    
    private List<MySQLPreparedStatementParameterType> getNewParameterTypes(final int parameterCount) {
        List<MySQLPreparedStatementParameterType> result = new ArrayList<>(parameterCount);
        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
            MySQLBinaryColumnType columnType = MySQLBinaryColumnType.valueOf(payload.readInt1());
            int unsignedFlag = payload.readInt1();
            result.add(new MySQLPreparedStatementParameterType(columnType, unsignedFlag));
        }
        return result;
    }
    
    /**
     * Read parameter values from packet.
     *
     * @param parameterTypes parameter type of values
     * @param longDataIndexes indexes of long data
     * @return parameter values
     * @throws SQLException SQL exception
     */
    public List<Object> readParameters(final List<MySQLPreparedStatementParameterType> parameterTypes, final Set<Integer> longDataIndexes) throws SQLException {
        List<Object> result = new ArrayList<>(parameterTypes.size());
        for (int parameterIndex = 0; parameterIndex < parameterTypes.size(); parameterIndex++) {
            if (longDataIndexes.contains(parameterIndex)) {
                result.add(null);
                continue;
            }
            MySQLBinaryProtocolValue binaryProtocolValue = MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(parameterTypes.get(parameterIndex).getColumnType());
            result.add(nullBitmap.isNullParameter(parameterIndex) ? null : binaryProtocolValue.read(payload));
        }
        return result;
    }
}
