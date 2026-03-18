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

import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

/**
 * Binary protocol value for int4 array for PostgreSQL.
 */
public final class PostgreSQLInt4ArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    private static final PostgreSQLArrayParameterDecoder ARRAY_PARAMETER_DECODER = new PostgreSQLArrayParameterDecoder();
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        throw new UnsupportedSQLOperationException("PostgreSQLInt4ArrayBinaryProtocolValue.getColumnLength()");
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        return ARRAY_PARAMETER_DECODER.decodeInt4Array(bytes, '{' != bytes[0]);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        throw new UnsupportedSQLOperationException("PostgreSQLInt4ArrayBinaryProtocolValue.write()");
    }
}
