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
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Authentication OK packet for PostgreSQL.
 */
public final class PostgreSQLAuthenticationOKPacket implements PostgreSQLPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.AUTHENTICATION_OK.getValue();
    
    private final int success;
    
    public PostgreSQLAuthenticationOKPacket(final boolean isSuccess) {
        success = isSuccess ? 0 : 1;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt4(success);
    }
}
