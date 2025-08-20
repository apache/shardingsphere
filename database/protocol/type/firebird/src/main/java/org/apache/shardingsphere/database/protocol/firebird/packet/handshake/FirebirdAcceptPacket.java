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
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.List;

/**
 * Accept packet for Firebird.
 */
@Getter
public final class FirebirdAcceptPacket extends FirebirdPacket {
    
    private static final int BATCH_SEND = 3;
    
    private static final int COMPRESS = 0x100;
    
    private static final int LAZY_SEND = 5;
    
    private static final int MASK = 0xFF;
    
    private static final int OUT_OF_BAND = 4;
    
    private FirebirdCommandPacketType opCode;
    
    private FirebirdProtocol protocol;
    
    private FirebirdAcceptDataPacket acceptDataPacket;
    
    public FirebirdAcceptPacket(final List<FirebirdProtocol> userProtocols) {
        opCode = FirebirdCommandPacketType.ACCEPT;
        protocol = userProtocols.remove(0);
        for (FirebirdProtocol protocol : userProtocols) {
            if (FirebirdArchType.isValid(protocol.getArch())
                    && protocol.getWeight() >= this.protocol.getWeight()) {
                this.protocol = protocol;
            }
        }
    }
    
    /**
     * Set accept data packet.
     *
     * @param salt salt value
     * @param publicKey public key
     * @param plugin authentication plugin
     * @param authenticated authentication flag
     * @param keys additional keys
     */
    public void setAcceptDataPacket(final byte[] salt,
                                    final String publicKey,
                                    final FirebirdAuthenticationMethod plugin,
                                    final int authenticated,
                                    final String keys) {
        acceptDataPacket = new FirebirdAcceptDataPacket(salt, publicKey, plugin, authenticated, keys);
        opCode = FirebirdCommandPacketType.ACCEPT_DATA;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        // Operation code
        payload.writeInt4(opCode.getValue());
        // Protocol version
        payload.writeInt4(protocol.getVersion().getCode());
        // Architecture type
        payload.writeInt4(protocol.getArch().getCode());
        // Minimum type
        int type = Math.min(protocol.getMaxType() & MASK, LAZY_SEND);
        int compress = protocol.getMaxType() & COMPRESS;
        payload.writeInt4(type | compress);
        
        if (null != acceptDataPacket) {
            acceptDataPacket.write(payload);
        }
    }
}
