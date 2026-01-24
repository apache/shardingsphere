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
import org.postgresql.util.PGbytea;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * ByteaArrayEncoder.
 */
public final class ByteaArrayEncoder extends AbstractArrayEncoder<byte[]> {
    
    public static final ByteaArrayEncoder INSTANCE = new ByteaArrayEncoder();
    
    private ByteaArrayEncoder() {
        super(Oid.BYTEA);
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void write(final byte[] item, final ByteArrayOutputStream bout, final Charset charset) {
        int length = item.length;
        bout.write((byte) (length >>> 24));
        bout.write((byte) (length >>> 16));
        bout.write((byte) (length >>> 8));
        bout.write(length);
        bout.write(item);
    }
    
    @Override
    public String toString(final byte[] item) {
        if (item == null) {
            return "NULL";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"\\\\x");
        PGbytea.appendHexString(sb, item, 0, item.length);
        sb.append('"');
        return sb.toString();
    }
}
