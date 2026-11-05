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

import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.StandardCharsets;

/**
 * Continue authentication packet for Firebird.
 */
@Getter
public final class FirebirdContinueAuthPacket extends FirebirdPacket {
    
    private final byte[] data;
    
    private final String pluginName;
    
    private final String pluginList;
    
    private final byte[] keys;
    
    public FirebirdContinueAuthPacket(final FirebirdPacketPayload payload) {
        data = ByteBufUtil.getBytes(payload.readBuffer());
        pluginName = payload.readBuffer().toString(StandardCharsets.US_ASCII);
        pluginList = payload.readBuffer().toString(StandardCharsets.US_ASCII);
        keys = ByteBufUtil.getBytes(payload.readBuffer());
    }
    
    public FirebirdContinueAuthPacket(final byte[] data, final String pluginName, final String pluginList, final byte[] keys) {
        this.data = data;
        this.pluginName = pluginName;
        this.pluginList = pluginList;
        this.keys = keys;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.CONT_AUTH.getValue());
        payload.writeBuffer(data);
        payload.writeBuffer(pluginName.getBytes(StandardCharsets.US_ASCII));
        payload.writeBuffer(pluginList.getBytes(StandardCharsets.US_ASCII));
        payload.writeBuffer(keys);
    }
}
