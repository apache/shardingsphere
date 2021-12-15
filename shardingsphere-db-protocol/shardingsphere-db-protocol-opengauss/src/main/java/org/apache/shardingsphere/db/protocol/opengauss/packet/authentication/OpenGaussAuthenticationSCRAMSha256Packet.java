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

package org.apache.shardingsphere.db.protocol.opengauss.packet.authentication;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Authentication request SCRAM SHA-256 for openGauss.
 */
@RequiredArgsConstructor
public final class OpenGaussAuthenticationSCRAMSha256Packet implements PostgreSQLIdentifierPacket {
    
    private static final int AUTH_REQ_SHA256 = 10;
    
    private static final int PASSWORD_STORED_METHOD_SHA256 = 2;
    
    private final byte[] random64Code;
    
    private final byte[] token;
    
    private final int serverIteration;
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt4(AUTH_REQ_SHA256);
        payload.writeInt4(PASSWORD_STORED_METHOD_SHA256);
        payload.writeBytes(random64Code);
        payload.writeBytes(token);
        payload.writeInt4(serverIteration);
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST;
    }
}
