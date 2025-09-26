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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder;

import lombok.SneakyThrows;
import org.postgresql.core.Oid;
import org.postgresql.jdbc.PgArray;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * StringArrayEncoder.
 */
public final class StringArrayEncoder extends AbstractArrayEncoder<String> {
    
    public static final StringArrayEncoder INSTANCE = new StringArrayEncoder(Oid.TEXT);
    
    private StringArrayEncoder(final int oid) {
        super(oid);
    }
    
    @Override
    int getTypeOID(final int arrayOid) {
        if (Oid.VARCHAR_ARRAY == arrayOid) {
            return Oid.VARCHAR;
        }
        if (Oid.TEXT_ARRAY == arrayOid) {
            return Oid.TEXT;
        }
        throw new IllegalArgumentException("unknown array type: " + arrayOid);
    }
    
    @SneakyThrows
    @Override
    public void write(final String item, final ByteArrayOutputStream bout, final Charset charset) {
        byte[] bytes = item.getBytes(charset);
        int length = bytes.length;
        bout.write((byte) (length >>> 24));
        bout.write((byte) (length >>> 16));
        bout.write((byte) (length >>> 8));
        bout.write(length);
        bout.write(bytes);
    }
    
    @Override
    public String toString(final String item) {
        StringBuilder sb = new StringBuilder();
        PgArray.escapeArrayElement(sb, item);
        return sb.toString();
    }
}
