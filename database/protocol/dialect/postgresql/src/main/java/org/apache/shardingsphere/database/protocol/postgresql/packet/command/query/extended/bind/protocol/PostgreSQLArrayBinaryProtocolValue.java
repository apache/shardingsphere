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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder.PgBinaryObj;
import org.postgresql.core.Oid;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.ShardingSpherePgArrayUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    public static final PostgreSQLArrayBinaryProtocolValue INSTANCE = new PostgreSQLArrayBinaryProtocolValue();
    
    private static final Map<Integer, String> OID_TYPE_NAME = new HashMap<>();
    
    static {
        OID_TYPE_NAME.put(Oid.BOOL, "bool[]");
        OID_TYPE_NAME.put(Oid.BYTEA, "bytea[]");
        // TODO not supported yet
        // oidTypeName.put(Oid.CHAR_ARRAY, "char[]");
        // oidTypeName.put(Oid.NAME_ARRAY, "name[]");
        OID_TYPE_NAME.put(Oid.INT2, "int2[]");
        OID_TYPE_NAME.put(Oid.INT4, "int4[]");
        OID_TYPE_NAME.put(Oid.INT8, "int8[]");
        OID_TYPE_NAME.put(Oid.FLOAT4, "float4[]");
        OID_TYPE_NAME.put(Oid.FLOAT8, "float8[]");
        OID_TYPE_NAME.put(Oid.TEXT, "text[]");
        OID_TYPE_NAME.put(Oid.VARCHAR, "varchar[]");
        OID_TYPE_NAME.put(Oid.DATE, "date[]");
        OID_TYPE_NAME.put(Oid.TIMESTAMP, "timestamp[]");
        // TODO not supported yet
        // oidTypeName.put(Oid.TIMESTAMPTZ_ARRAY, "timestamptz[]");
        OID_TYPE_NAME.put(Oid.TIME, "time[]");
        // TODO not supported yet
        // oidTypeName.put(Oid.TIMETZ_ARRAY, "timetz[]");
        OID_TYPE_NAME.put(Oid.NUMERIC, "numeric[]");
        // TODO not supported yet
        // oidTypeName.put(Oid.UUID_ARRAY, "uuid[]");
    }
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        return -1;
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int oid = buf.getInt(8);
        String typeName = OID_TYPE_NAME.get(oid);
        PgBinaryObj result = new PgBinaryObj(bytes);
        result.setType(typeName);
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        byte[] result = ShardingSpherePgArrayUtils.getBinaryBytes(value, payload.getCharset());
        payload.writeInt4(result.length);
        payload.writeBytes(result);
    }
}
