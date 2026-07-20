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
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Binary protocol value for BLOB for Firebird.
 */
public final class FirebirdBlobBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    private static final AtomicLong ID_SEQ = new AtomicLong(1L);
    
    private static final Map<Integer, Map<Long, byte[]>> CONTENTS_BY_CONNECTION = new ConcurrentHashMap<>();
    
    /**
     * Get stored BLOB content by connection id and internal BLOB id.
     *
     * @param connectionId connection id
     * @param blobId internal BLOB id
     * @return stored bytes or {@code null} if missing
     */
    public static byte[] getBlobContent(final int connectionId, final long blobId) {
        Map<Long, byte[]> connectionContents = CONTENTS_BY_CONNECTION.get(connectionId);
        return null == connectionContents ? null : connectionContents.get(blobId);
    }
    
    /**
     * Unregister connection and drop all BLOB contents stored for it.
     *
     * @param connectionId connection id
     */
    public static void unregisterConnection(final int connectionId) {
        CONTENTS_BY_CONNECTION.remove(connectionId);
    }
    
    private static long register(final int connectionId, final byte[] bytes) {
        long id = ID_SEQ.getAndIncrement();
        getContentMap(connectionId).put(id, bytes.clone());
        return id;
    }
    
    private static Map<Long, byte[]> getContentMap(final int connectionId) {
        Map<Long, byte[]> result = CONTENTS_BY_CONNECTION.get(connectionId);
        return null == result ? CONTENTS_BY_CONNECTION.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4)) : result;
    }
    
    private static byte[] readAllBytes(final InputStream input) throws IOException {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) {
                if (n > 0) {
                    out.write(buf, 0, n);
                }
            }
            return out.toByteArray();
        }
    }
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        return payload.readInt8();
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        long blobId;
        
        if (null == value) {
            blobId = 0L;
        } else if (value instanceof Long) {
            blobId = (Long) value;
        } else if (value instanceof byte[]) {
            blobId = register(payload.getConnectionId(), (byte[]) value);
        } else if (value instanceof Blob) {
            try {
                blobId = register(payload.getConnectionId(), readAllBytes(((Blob) value).getBinaryStream()));
            } catch (final SQLException ex) {
                throw new IllegalStateException("Failed to read java.sql.Blob stream", ex);
            } catch (final IOException ex) {
                throw new IllegalStateException("Failed to read java.sql.Blob content", ex);
            }
        } else if (value instanceof Clob) {
            try {
                Clob clob = (Clob) value;
                int len = (int) Math.min(Integer.MAX_VALUE, clob.length());
                String str = clob.getSubString(1L, len);
                blobId = register(payload.getConnectionId(), str.getBytes(payload.getCharset()));
            } catch (final SQLException ex) {
                throw new IllegalStateException("Failed to read java.sql.Clob", ex);
            }
        } else {
            blobId = register(payload.getConnectionId(), value.toString().getBytes(payload.getCharset()));
        }
        
        payload.writeInt8(blobId);
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 8;
    }
}
