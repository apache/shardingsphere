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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder.PgBinaryObj;
import org.postgresql.core.Oid;
import org.postgresql.jdbc.ShardingSpherePgArrayUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PostgreSQLArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    private static final Map<Integer, String> oidTypeName = new HashMap<>();
    
    static {
        oidTypeName.put(Oid.BOOL, "bool[]");
        oidTypeName.put(Oid.BYTEA, "bytea[]");
        // oidTypeName.put(Oid.CHAR_ARRAY, "char[]");
        // oidTypeName.put(Oid.NAME_ARRAY, "name[]");
        oidTypeName.put(Oid.INT2, "int2[]");
        oidTypeName.put(Oid.INT4, "int4[]");
        oidTypeName.put(Oid.INT8, "int8[]");
        oidTypeName.put(Oid.FLOAT4, "float4[]");
        oidTypeName.put(Oid.FLOAT8, "float8[]");
        oidTypeName.put(Oid.TEXT, "text[]");
        oidTypeName.put(Oid.VARCHAR, "varchar[]");
        oidTypeName.put(Oid.DATE, "date[]");
        oidTypeName.put(Oid.TIMESTAMP, "timestamp[]");
        // oidTypeName.put(Oid.TIMESTAMPTZ_ARRAY, "timestamptz[]");
        oidTypeName.put(Oid.TIME, "time[]");
        // oidTypeName.put(Oid.TIMETZ_ARRAY, "timetz[]");
        oidTypeName.put(Oid.NUMERIC, "numeric[]");
        // oidTypeName.put(Oid.UUID_ARRAY, "uuid[]");
    }
    
    private PostgreSQLArrayBinaryProtocolValue() {
        
    }
    public static final PostgreSQLArrayBinaryProtocolValue instance = new PostgreSQLArrayBinaryProtocolValue();
    
    @Override
    public int getColumnLength(PostgreSQLPacketPayload payload, Object value) {
        return -1;
    }
    
    @Override
    public Object read(PostgreSQLPacketPayload payload, int parameterValueLength) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int oid = buf.getInt(8);
        String typeName = oidTypeName.get(oid);
        PgBinaryObj pgBinaryObj = new PgBinaryObj(bytes);
        pgBinaryObj.setType(typeName);
        return pgBinaryObj;
    }
    
    @Override
    public void write(PostgreSQLPacketPayload payload, Object value) {
        byte[] result = ShardingSpherePgArrayUtils.getBinaryBytes(value, payload.getCharset());
        payload.writeInt4(result.length);
        payload.writeBytes(result);
    }
}
