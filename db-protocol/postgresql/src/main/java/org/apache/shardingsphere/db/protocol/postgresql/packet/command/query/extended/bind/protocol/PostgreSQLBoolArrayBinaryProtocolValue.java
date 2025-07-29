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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;


/**
 * Binary protocol value for boolean array for PostgreSQL.
 */
public final class PostgreSQLBoolArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    private static final PostgreSQLArrayParameterDecoder ARRAY_PARAMETER_DECODER = new PostgreSQLArrayParameterDecoder();
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        throw new UnsupportedSQLOperationException("PostgreSQLBoolArrayBinaryProtocolValue.getColumnLength()");
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        return ARRAY_PARAMETER_DECODER.decodeBoolArray(bytes, '{' != bytes[0]);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        StringBuilder result = new StringBuilder("{");
        List<String> boolStrings = new ArrayList<>();

        if (value instanceof boolean[]) {
            for (boolean b : (boolean[]) value) {
                boolStrings.add(b ? "t" : "f");
            }
        } else if (value instanceof Boolean[]) {
            for (Boolean b : (Boolean[]) value) {
                boolStrings.add(Boolean.TRUE.equals(b) ? "t" : "f");
            }
        } else {
            throw new UnsupportedSQLOperationException("Unsupported type for PostgreSQLBoolArrayBinaryProtocolValue.write()");
        }

        result.append(String.join(",", boolStrings)).append("}");
        byte[] bytes = result.toString().getBytes(StandardCharsets.UTF_8);
        payload.getByteBuf().writeInt(bytes.length);
        payload.getByteBuf().writeBytes(bytes);
    }
}
