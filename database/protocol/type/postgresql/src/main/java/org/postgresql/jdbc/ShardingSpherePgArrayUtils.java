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

package org.postgresql.jdbc;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLArrayParameterDecoder;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder.ArrayEncoding;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder.ArrayEncoder;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Oid;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;

public class ShardingSpherePgArrayUtils {
    
    /**
     * encode pgArray.
     * @param array PgArrayObject
     * @param charset charset
     * @return binary bytes array
     * @throws UnsupportedSQLOperationException if array is not PgArray
     */
    @SneakyThrows(SQLException.class)
    public static byte[] getBinaryBytes(final Object array, final Charset charset) {
        PgArray pgArray;
        if (array instanceof PgArray) {
            pgArray = (PgArray) array;
        } else {
            throw new UnsupportedSQLOperationException("can not encode" + array.getClass());
        }
        byte[] bytes = pgArray.toBytes();
        
        if (bytes != null) {
            return bytes;
        }
        BaseConnection connection = pgArray.connection;
        
        assert connection != null;
        int pgType = connection.getTypeInfo().getPGArrayType(pgArray.getBaseTypeName());
        
        Object realArray;
        if (pgType == Oid.NUMERIC_ARRAY) {
            realArray = new PostgreSQLArrayParameterDecoder().decodeNumberArray(pgArray.fieldString);
        } else {
            realArray = pgArray.getArray();
        }
        
        final ArrayEncoder arraySupport = ArrayEncoding.getArrayEncoder(realArray);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        arraySupport.toBinaryRepresentation(realArray, pgType, byteArrayOutputStream, charset);
        return byteArrayOutputStream.toByteArray();
        
    }
    
}
