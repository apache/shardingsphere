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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdColumnType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Firebird allocate statement packet.
 */
@Getter
public final class FirebirdExecuteStatementPacket extends FirebirdCommandPacket {

    private final int statementId;
    private final int transactionId;
    private final ByteBuf blrParams;
    private final int message;
    private final List<ByteBuf> paramsValues = new ArrayList<>();

    public FirebirdExecuteStatementPacket(FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        statementId = payload.readInt4();
        transactionId = payload.readInt4();
        blrParams = payload.readBuffer();
        message = payload.readInt4();
        int msgCount = payload.readInt4();
        while (msgCount-- != 0) {
            paramsValues.add(payload.readBuffer());
        }
    }

    public List<FirebirdColumnType> getParameterTypes() {
        //TODO parse BLR parameters
        return new ArrayList<>(0);
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
    public List<Object> readParameters(final List<FirebirdColumnType> paramTypes, final Set<Integer> longDataIndexes) throws SQLException {
        List<Object> result = new ArrayList<>(paramTypes.size());
        //TODO parse parameter values
//        for (int paramIndex = 0; paramIndex < paramTypes.size(); paramIndex++) {
//            if (longDataIndexes.contains(paramIndex)) {
//                result.add(null);
//                continue;
//            }
//            MySQLBinaryProtocolValue binaryProtocolValue = MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(paramTypes.get(paramIndex).getColumnType());
//            Object value = nullBitmap.isNullParameter(paramIndex) ? null
//                    : binaryProtocolValue.read(payload, (parameterFlags.get(paramIndex) & MySQLColumnDefinitionFlag.UNSIGNED.getValue()) == MySQLColumnDefinitionFlag.UNSIGNED.getValue());
//            result.add(value);
//        }
        return result;
    }

    @Override
    protected void write(final FirebirdPacketPayload payload) {}
}
