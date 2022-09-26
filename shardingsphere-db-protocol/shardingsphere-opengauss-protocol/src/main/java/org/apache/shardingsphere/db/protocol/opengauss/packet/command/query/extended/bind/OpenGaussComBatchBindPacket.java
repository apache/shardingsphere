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

package org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacket;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.db.protocol.opengauss.packet.identifier.OpenGaussIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLTextTimestampUtils;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch bind packet for openGauss.
 */
@Getter
@ToString
public final class OpenGaussComBatchBindPacket extends OpenGaussCommandPacket {
    
    private final PostgreSQLPacketPayload payload;
    
    private final int batchNum;
    
    private final String statementId;
    
    private final List<Integer> parameterFormats;
    
    private final List<PostgreSQLValueFormat> resultFormats;
    
    private final int eachGroupParametersCount;
    
    public OpenGaussComBatchBindPacket(final PostgreSQLPacketPayload payload) {
        this.payload = payload;
        payload.readInt4();
        batchNum = payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        int parameterFormatCount = payload.readInt2();
        parameterFormats = new ArrayList<>(parameterFormatCount);
        for (int i = 0; i < parameterFormatCount; i++) {
            parameterFormats.add(payload.readInt2());
        }
        int resultFormatsLength = payload.readInt2();
        resultFormats = new ArrayList<>(resultFormatsLength);
        for (int i = 0; i < resultFormatsLength; i++) {
            resultFormats.add(PostgreSQLValueFormat.valueOf(payload.readInt2()));
        }
        eachGroupParametersCount = payload.readInt2();
    }
    
    /**
     * Read parameter sets from payload.
     *
     * @param parameterTypes types of parameters
     * @return parameter sets
     */
    public List<List<Object>> readParameterSets(final List<PostgreSQLColumnType> parameterTypes) {
        List<List<Object>> result = new ArrayList<>(batchNum);
        for (int i = 0; i < batchNum; i++) {
            result.add(readOneGroupOfParameters(parameterTypes));
        }
        payload.skipReserved(payload.getByteBuf().readableBytes());
        return result;
    }
    
    private List<Object> readOneGroupOfParameters(final List<PostgreSQLColumnType> parameterTypes) {
        List<Object> result = new ArrayList<>(eachGroupParametersCount);
        for (int parameterIndex = 0; parameterIndex < eachGroupParametersCount; parameterIndex++) {
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
        return parameterFormats.isEmpty() || 0 == parameterFormats.get(parameterIndex % parameterFormats.size());
    }
    
    private Object getTextParameters(final PostgreSQLPacketPayload payload, final int parameterValueLength, final PostgreSQLColumnType parameterType) {
        String value = payload.getByteBuf().readCharSequence(parameterValueLength, payload.getCharset()).toString();
        return getTextParameters(value, parameterType);
    }
    
    private Object getTextParameters(final String textValue, final PostgreSQLColumnType columnType) {
        switch (columnType) {
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
    
    private Object getBinaryParameters(final PostgreSQLPacketPayload payload, final int parameterValueLength, final PostgreSQLColumnType columnType) {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(columnType);
        return binaryProtocolValue.read(payload, parameterValueLength);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public OpenGaussIdentifierTag getIdentifier() {
        return OpenGaussCommandPacketType.BATCH_BIND_COMMAND;
    }
}
