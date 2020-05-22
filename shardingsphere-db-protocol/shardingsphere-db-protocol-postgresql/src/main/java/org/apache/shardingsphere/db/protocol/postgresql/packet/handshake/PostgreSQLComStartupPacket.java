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

package org.apache.shardingsphere.db.protocol.postgresql.packet.handshake;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Startup packet for PostgreSQL.
 */
@Getter
public final class PostgreSQLComStartupPacket implements PostgreSQLPacket {
    
    private final char messageType = '\0';
    
    private final Map<String, String> parametersMap = new HashMap<>(16, 1);
    
    public PostgreSQLComStartupPacket(final PostgreSQLPacketPayload payload) {
        payload.skipReserved(8);
        while (0 != payload.bytesBeforeZero()) {
            parametersMap.put(payload.readStringNul(), payload.readStringNul());
        }
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
}
