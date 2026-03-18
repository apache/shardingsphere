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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.postgresql.util.PGobject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Binary protocol value for text array for PostgreSQL.
 */
@Slf4j
public final class PostgreSQLTextArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    private static final int ARRAY_HEADER_LENGTH = 20;
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        throw new UnsupportedSQLOperationException("PostgreSQLTextArrayBinaryProtocolValue.getColumnLength()");
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        if (ARRAY_HEADER_LENGTH == parameterValueLength) {
            return getTextArray(Collections.emptyList());
        }
        byte[] header = new byte[ARRAY_HEADER_LENGTH];
        payload.getByteBuf().readBytes(header);
        byte[] dataBytes = new byte[parameterValueLength - ARRAY_HEADER_LENGTH];
        payload.getByteBuf().readBytes(dataBytes);
        return getTextArray(extractArrayElements(dataBytes));
    }
    
    private Object getTextArray(final Collection<String> elements) {
        try {
            PGobject result = new PGobject();
            result.setType("text[]");
            result.setValue(getArrayString(elements));
            return result;
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private String getArrayString(final Collection<String> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<String> iterator = elements.iterator();
        while (iterator.hasNext()) {
            sb.append("\"").append(iterator.next()).append("\"");
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private Collection<String> extractArrayElements(final byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Collection<String> result = new LinkedList<>();
        while (buffer.remaining() > 0) {
            int length = buffer.getInt();
            if (buffer.remaining() < length) {
                log.warn("cannot read the complete data packet, remaining: {}, expected: {}", buffer.remaining(), length);
                break;
            }
            byte[] packetData = new byte[length];
            buffer.get(packetData);
            result.add(new String(packetData, StandardCharsets.UTF_8));
        }
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        throw new UnsupportedSQLOperationException("PostgreSQLTextArrayBinaryProtocolValue.write()");
    }
}
