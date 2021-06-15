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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command bind packet for PostgreSQL.
 */
@Getter
public final class PostgreSQLComBindPacket extends PostgreSQLCommandPacket {
    
    private final String statementId;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final List<Integer> resultFormatCodes;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload, final int connectionId) {
        payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        int parameterFormatCount = payload.readInt2();
        List<Integer> parameterFormats = new ArrayList<>(parameterFormatCount);
        for (int i = 0; i < parameterFormatCount; i++) {
            parameterFormats.add(payload.readInt2());
        }
        PostgreSQLBinaryStatement binaryStatement = PostgreSQLBinaryStatementRegistry.getInstance().get(connectionId).getBinaryStatement(statementId);
        sql = null == binaryStatement ? null : binaryStatement.getSql();
        parameters = null == sql ? Collections.emptyList() : getParameters(payload, parameterFormats, binaryStatement.getColumnTypes());
        int resultFormatsLength = payload.readInt2();
        resultFormatCodes = new ArrayList<>(resultFormatsLength);
        for (int i = 0; i < resultFormatsLength; i++) {
            resultFormatCodes.add(payload.readInt2());
        }
    }
    
    private List<Object> getParameters(final PostgreSQLPacketPayload payload, final List<Integer> parameterFormats, final List<PostgreSQLBinaryColumnType> columnTypes) {
        int parameterCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parameterCount);
        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
            int parameterValueLength = payload.readInt4();
            if (-1 == parameterValueLength) {
                result.add(null);
                continue;
            }
            Object parameterValue = isTextParameterValue(parameterFormats, parameterIndex)
                    ? getTextParameters(payload, parameterValueLength, columnTypes.get(parameterIndex)) : getBinaryParameters(payload, parameterValueLength, columnTypes.get(parameterIndex));
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
    
    private Object getTextParameters(final PostgreSQLPacketPayload payload, final int parameterValueLength, final PostgreSQLBinaryColumnType columnType) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        return getTextParameters(new String(bytes), columnType);
    }

    private Object getTextParameters(final String textValue, final PostgreSQLBinaryColumnType columnType) {
        switch (columnType) {
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
                return Timestamp.valueOf(textValue);
            default:
                return textValue;
        }
    }
    
    private Object getBinaryParameters(final PostgreSQLPacketPayload payload, final int parameterValueLength, final PostgreSQLBinaryColumnType columnType) {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(columnType);
        return binaryProtocolValue.read(payload, parameterValueLength);
    }
    
    /**
     * Get result format by column index.
     *
     * @param columnIndex column index
     * @return result format
     */
    public PostgreSQLColumnFormat getResultFormatByColumnIndex(final int columnIndex) {
        if (resultFormatCodes.isEmpty()) {
            return PostgreSQLColumnFormat.TEXT;
        }
        if (1 == resultFormatCodes.size()) {
            return PostgreSQLColumnFormat.valueOf(resultFormatCodes.get(0));
        }
        return PostgreSQLColumnFormat.valueOf(resultFormatCodes.get(columnIndex));
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLCommandPacketType.BIND_COMMAND;
    }
}
