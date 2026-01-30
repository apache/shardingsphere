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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Binary protocol value for blob for Firebird.
 */
public final class FirebirdBlobBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    private static final AtomicLong ID_SEQ = new AtomicLong(1L);
    
    private static final Map<Long, byte[]> STORE = new ConcurrentHashMap<>();
    
    public static byte[] getBlobContent(final long blobId) {
        return STORE.get(blobId);
    }
    
    public static void removeBlobContent(final long blobId) {
        STORE.remove(blobId);
    }
    
    private static long register(final byte[] bytes) {
        long id = ID_SEQ.getAndIncrement();
        STORE.put(id, null == bytes ? new byte[0] : Arrays.copyOf(bytes, bytes.length));
        return id;
    }
    
    private static byte[] readAllBytes(final InputStream input) {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) {
                if (n > 0) {
                    out.write(buf, 0, n);
                }
            }
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read blob stream", ex);
        }
    }
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        byte[] bytes = new byte[payload.readInt4()];
        payload.getByteBuf().readBytes(bytes);
        payload.skipPadding(bytes.length);
        String s = new String(bytes, payload.getCharset());
        return s;
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        long blobId;
        
        if (null == value) {
            blobId = 0L;
        } else if (value instanceof Long) {
            blobId = (Long) value;
        } else if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            blobId = register(bytes);
        } else if (value instanceof Blob) {
            try {
                blobId = register(readAllBytes(((Blob) value).getBinaryStream()));
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to read java.sql.Blob", ex);
            }
        } else if (value instanceof Clob) {
            try {
                Clob clob = (Clob) value;
                int len = (int) Math.min(Integer.MAX_VALUE, clob.length());
                String str = clob.getSubString(1L, len);
                blobId = register(str.getBytes(payload.getCharset()));
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to read java.sql.Clob", ex);
            }
        } else {
            blobId = register(value.toString().getBytes(payload.getCharset()));
        }
        
        payload.writeInt8(blobId);
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 8;
    }
}
