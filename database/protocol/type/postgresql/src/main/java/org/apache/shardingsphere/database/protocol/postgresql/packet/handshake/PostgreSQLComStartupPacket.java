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

package org.apache.shardingsphere.database.protocol.postgresql.packet.handshake;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Startup packet for PostgreSQL.
 */
public final class PostgreSQLComStartupPacket extends PostgreSQLPacket {
    
    private static final String DATABASE_NAME_KEY = "database";
    
    private static final String USER_NAME_KEY = "user";
    
    private static final String CLIENT_ENCODING_KEY = "client_encoding";
    
    @Getter
    private final int version;
    
    private final Map<String, String> parametersMap = new HashMap<>();
    
    public PostgreSQLComStartupPacket(final PostgreSQLPacketPayload payload) {
        payload.skipReserved(4);
        version = payload.readInt4();
        while (payload.bytesBeforeZero() > 0) {
            parametersMap.put(payload.readStringNul(), payload.readStringNul());
        }
    }
    
    /**
     * Get database.
     *
     * @return database
     */
    public String getDatabase() {
        return parametersMap.get(DATABASE_NAME_KEY);
    }
    
    /**
     * Get username.
     *
     * @return username
     */
    public String getUsername() {
        return parametersMap.get(USER_NAME_KEY);
    }
    
    /**
     * Get client encoding.
     *
     * @return client encoding
     */
    public String getClientEncoding() {
        return parametersMap.getOrDefault(CLIENT_ENCODING_KEY, "UTF8");
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
    }
}
