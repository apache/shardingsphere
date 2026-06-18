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

package org.apache.shardingsphere.database.protocol.firebird.packet.handshake;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.StandardCharsets;

/**
 * Accept data packet for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdAcceptDataPacket extends FirebirdPacket {
    
    private final byte[] salt;
    
    private final String publicKey;
    
    private final FirebirdAuthenticationMethod plugin;
    
    private final int authenticated;
    
    private final String keys;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        if (salt.length != 0 && !publicKey.isEmpty()) {
            payload.writeInt4(salt.length + publicKey.length() + 4);
            payload.writeInt2LE(salt.length);
            payload.writeBytes(salt);
            payload.writeInt2LE(publicKey.length());
            payload.writeBytes(publicKey.getBytes(StandardCharsets.US_ASCII));
        } else {
            payload.writeInt4(0);
        }
        payload.writeString(plugin.getMethodName());
        payload.writeInt4(authenticated);
        payload.writeString(keys);
    }
}
