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

package org.apache.shardingsphere.db.protocol.firebird.packet.handshake;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.List;

/**
 * Accept packet for Firebird.
 */
@Getter
public final class FirebirdAcceptPacket extends FirebirdPacket {

    FirebirdCommandPacketType opCode;
    private FirebirdProtocol protocol;
    private FirebirdAcceptDataPacket acceptDataPacket;

    private final static int BATCH_SEND     = 3;	// Batch sends, no asynchrony
    private final static int OUT_OF_BAND    = 4;	// Batch sends w/ out of band notification
    private final static int LAZY_SEND      = 5;	// Deferred packets delivery
    private final static int MASK           = 0xFF; // Mask - up to 255 types of protocol
    // upper byte is used for protocol flags
    private final static int COMPRESS       = 0x100; // Turn on compression if possible

    public FirebirdAcceptPacket(final List<FirebirdProtocol> userProtocols) {
        opCode = FirebirdCommandPacketType.OP_ACCEPT;
        boolean accepted = false;
        for (FirebirdProtocol protocol : userProtocols) {
            if (FirebirdArchType.isValid(protocol.getArch()) &&
                    (!accepted || protocol.getWeight() >= this.protocol.getWeight())) {
                accepted = true;
                this.protocol = protocol;
            }
        }
        Preconditions.checkNotNull(this.protocol, "None of client protocols is accepted");
    }

    public void setAcceptDataPacket(final byte[] salt,
                                    final String publicKey,
                                    final FirebirdAuthenticationMethod plugin,
                                    final int authenticated,
                                    final String keys) {
        acceptDataPacket = new FirebirdAcceptDataPacket(salt, publicKey, plugin, authenticated, keys);
        opCode = FirebirdCommandPacketType.OP_ACCEPT_DATA;
    }

    @Override
    protected void write(FirebirdPacketPayload payload) {
        //Operation code
        payload.writeInt4(opCode.getValue());
        //Protocol version
        payload.writeInt4(protocol.getVersion().getCode());
        //Architecture type
        payload.writeInt4(protocol.getArch().getCode());
        //Minimum type
        int type = Math.min(protocol.getMaxType() & MASK, LAZY_SEND);
        int compress = protocol.getMaxType() & COMPRESS;
        payload.writeInt4(type | compress);

        if (acceptDataPacket != null) acceptDataPacket.write(payload);
    }
}