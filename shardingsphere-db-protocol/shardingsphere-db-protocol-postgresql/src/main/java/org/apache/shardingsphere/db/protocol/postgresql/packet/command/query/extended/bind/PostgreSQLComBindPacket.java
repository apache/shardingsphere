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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLTextTimestampUtils;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command bind packet for PostgreSQL.
 */
@Getter
@ToString
public final class PostgreSQLComBindPacket extends PostgreSQLCommandPacket {
    
    private final PostgreSQLPacketPayload payload;
    
    private final String portal;
    
    private final String statementId;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload) {
        this.payload = payload;
        payload.readInt4();
        portal = payload.readStringNul();
        statementId = payload.readStringNul();
    }
    
    /**
     * Read parameters from Bind message.
     *
     * @param parameterTypes parameter types
     * @return values of parameter
     */
    public List<Object> readParameters(final List<PostgreSQLColumnType> parameterTypes) {
        int parameterFormatCount = payload.readInt2();
        List<Integer> parameterFormats = new ArrayList<>(parameterFormatCount);
        for (int i = 0; i < parameterFormatCount; i++) {
            parameterFormats.add(payload.readInt2());
        }
        int parameterCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parameterCount);
        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
            int parameterValueLength = payload.readInt4();
            if (-1 == parameterValueLength) {
                result.add(null);
                continue;
            }
            Object parameterValue = isTextParameterValue(parameterFormats, parameterIndex)
                    ? getTextParameters(payload, parameterValueLength, parameterTypes.get(parameterIndex))
                    : getBinaryParameters(payload, parameterValueLength, parameterTypes.get(parameterIndex));
            result.add(parameterValue);
        }
        return result;
    }
    
    private boolean isTextParameterValue(final List<Integer> parameterFormats, final int parameterIndex) {
        if (parameterFormats.isEmpty()) {
            return true;
        }
        if (1 == parameterFormats.size()) {
            return 0 == parameterFormats.get(0);
        }
        return 0 == parameterFormats.get(parameterIndex);
    }
    
    private Object getTextParameters(final PostgreSQLPacketPayload payload, final int parameterValueLength, final PostgreSQLColumnType parameterType) {
        String value = payload.getByteBuf().readCharSequence(parameterValueLength, payload.getCharset()).toString();
        return getTextParameters(value, parameterType);
    }
    
    private Object getTextParameters(final String textValue, final PostgreSQLColumnType parameterType) {
        switch (parameterType) {
            case POSTGRESQL_TYPE_UNSPECIFIED:
                return new PostgreSQLTypeUnspecifiedSQLParameter(textValue);
            case POSTGRESQL_TYPE_BOOL:
                return Boolean.valueOf(textValue);
            case POSTGRESQL_TYPE_INT2:
            case POSTGRESQL_TYPE_INT4:
                return Integer.parseInt(textValue);
            case POSTGRESQL_TYPE_INT8:
                return Long.parseLong(textValue);
            case POSTGRESQL_TYPE_FLOAT4:
                return Float.parseFloat(textValue);
            case POSTGRESQL_TYPE_FLOAT8:
                return Double.parseDouble(textValue);
            case POSTGRESQL_TYPE_NUMERIC:
                try {
                    return Integer.parseInt(textValue);
                } catch (final NumberFormatException ignored) {
                }
                try {
                    return Long.parseLong(textValue);
                } catch (final NumberFormatException ignored) {
                }
                return new BigDecimal(textValue);
            case POSTGRESQL_TYPE_DATE:
                return Date.valueOf(textValue);
            case POSTGRESQL_TYPE_TIME:
                return Time.valueOf(textValue);
            case POSTGRESQL_TYPE_TIMESTAMP:
            case POSTGRESQL_TYPE_TIMESTAMPTZ:
                return PostgreSQLTextTimestampUtils.parse(textValue);
            default:
                return textValue;
        }
    }
    
    private Object getBinaryParameters(final PostgreSQLPacketPayload payload, final int parameterValueLength, final PostgreSQLColumnType parameterType) {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(parameterType);
        return binaryProtocolValue.read(payload, parameterValueLength);
    }
    
    /**
     * Read result formats from Bind message.
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
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLCommandPacketType.BIND_COMMAND;
    }
}
