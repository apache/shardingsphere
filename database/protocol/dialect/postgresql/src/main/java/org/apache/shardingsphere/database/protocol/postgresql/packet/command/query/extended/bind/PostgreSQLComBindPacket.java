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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command bind packet for PostgreSQL.
 */
public final class PostgreSQLComBindPacket extends PostgreSQLCommandPacket {
    
    private final PostgreSQLPacketPayload payload;
    
    @Getter
    private final String portal;
    
    @Getter
    private final String statementId;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload) {
        this.payload = payload;
        payload.readInt4();
        portal = payload.readStringNul();
        statementId = payload.readStringNul();
    }
    
    /**
     * Read parameters from bind message.
     *
     * @param paramTypes parameter types
     * @return values of parameter
     */
    public List<Object> readParameters(final List<PostgreSQLColumnType> paramTypes) {
        List<Integer> paramFormats = getParameterFormats();
        int parameterCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parameterCount);
        for (int paramIndex = 0; paramIndex < parameterCount; paramIndex++) {
            int parameterValueLength = payload.readInt4();
            if (-1 == parameterValueLength) {
                result.add(null);
                continue;
            }
            Object paramValue = isTextParameterValue(paramFormats, paramIndex)
                    ? getTextParameterValue(payload, parameterValueLength, paramTypes.get(paramIndex))
                    : getBinaryParameterValue(payload, parameterValueLength, paramTypes.get(paramIndex));
            result.add(paramValue);
        }
        return result;
    }
    
    private List<Integer> getParameterFormats() {
        int parameterFormatCount = payload.readInt2();
        List<Integer> result = new ArrayList<>(parameterFormatCount);
        for (int i = 0; i < parameterFormatCount; i++) {
            result.add(payload.readInt2());
        }
        return result;
    }
    
    private boolean isTextParameterValue(final List<Integer> paramFormats, final int paramIndex) {
        if (paramFormats.isEmpty()) {
            return true;
        }
        return PostgreSQLValueFormat.TEXT.getCode() == paramFormats.get(1 == paramFormats.size() ? 0 : paramIndex);
    }
    
    private Object getTextParameterValue(final PostgreSQLPacketPayload payload, final int paramValueLength, final PostgreSQLColumnType paramType) {
        String value = payload.getByteBuf().readCharSequence(paramValueLength, payload.getCharset()).toString();
        return paramType.getTextValueParser().parse(value);
    }
    
    private Object getBinaryParameterValue(final PostgreSQLPacketPayload payload, final int paramValueLength, final PostgreSQLColumnType paramType) {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(paramType);
        return binaryProtocolValue.read(payload, paramValueLength);
    }
    
    /**
     * Read result formats from bind message.
     *
     * @return formats of value
     */
    public List<PostgreSQLValueFormat> readResultFormats() {
        int resultFormatsLength = payload.readInt2();
        if (0 == resultFormatsLength) {
            return Collections.emptyList();
        }
        if (1 == resultFormatsLength) {
            return Collections.singletonList(PostgreSQLValueFormat.valueOf(payload.readInt2()));
        }
        List<PostgreSQLValueFormat> result = new ArrayList<>(resultFormatsLength);
        for (int i = 0; i < resultFormatsLength; i++) {
            result.add(PostgreSQLValueFormat.valueOf(payload.readInt2()));
        }
        return result;
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLCommandPacketType.BIND_COMMAND;
    }
}
