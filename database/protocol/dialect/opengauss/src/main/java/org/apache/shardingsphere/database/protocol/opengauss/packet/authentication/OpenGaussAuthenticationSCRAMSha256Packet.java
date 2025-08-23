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

package org.apache.shardingsphere.database.protocol.opengauss.packet.authentication;

import org.apache.shardingsphere.database.protocol.opengauss.constant.OpenGaussProtocolVersion;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Authentication request SCRAM SHA-256 for openGauss.
 */
public final class OpenGaussAuthenticationSCRAMSha256Packet extends PostgreSQLIdentifierPacket {
    
    private static final int AUTH_REQ_SHA256 = 10;
    
    private static final int PASSWORD_STORED_METHOD_SHA256 = 2;
    
    private final int version;
    
    private final int serverIteration;
    
    private final OpenGaussAuthenticationHexData authHexData;
    
    private final String serverSignature;
    
    public OpenGaussAuthenticationSCRAMSha256Packet(final int version, final int serverIteration, final OpenGaussAuthenticationHexData authHexData, final String password) {
        this.version = version;
        this.serverIteration = serverIteration;
        this.authHexData = authHexData;
        serverSignature = version >= OpenGaussProtocolVersion.PROTOCOL_350.getVersion() ? "" : OpenGaussMacCalculator.requestServerMac(password, authHexData, serverIteration);
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt4(AUTH_REQ_SHA256);
        payload.writeInt4(PASSWORD_STORED_METHOD_SHA256);
        payload.writeBytes(authHexData.getSalt().getBytes());
        payload.writeBytes(authHexData.getNonce().getBytes());
        if (version < OpenGaussProtocolVersion.PROTOCOL_350.getVersion()) {
            payload.writeBytes(serverSignature.getBytes());
        }
        if (OpenGaussProtocolVersion.PROTOCOL_351.getVersion() == version) {
            payload.writeInt4(serverIteration);
        }
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST;
    }
}
