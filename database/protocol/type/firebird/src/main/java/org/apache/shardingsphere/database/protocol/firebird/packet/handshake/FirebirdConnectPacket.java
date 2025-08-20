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

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdUserDataType;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Connect packet for Firebird.
 */
@Getter
public final class FirebirdConnectPacket extends FirebirdPacket {
    
    private final FirebirdCommandPacketType opCode;
    
    private final int connectVersion;
    
    private final FirebirdArchType archType;
    
    private final String database;
    
    private final int protocolsCount;
    
    private final Map<FirebirdUserDataType, String> userInfoMap = new EnumMap<>(FirebirdUserDataType.class);
    
    private final List<FirebirdProtocol> userProtocols = new ArrayList<>();
    
    public FirebirdConnectPacket(final FirebirdPacketPayload payload) {
        opCode = FirebirdCommandPacketType.valueOf(payload.readInt4());
        connectVersion = payload.readInt4();
        archType = FirebirdArchType.valueOf(payload.readInt4());
        database = payload.readString();
        protocolsCount = payload.readInt4();
        parseUserInfo(payload.readBuffer());
        parseProtocols(payload.getByteBuf());
    }
    
    private void parseUserInfo(final ByteBuf userInfo) {
        SortedMap<Integer, String> pendingData = new TreeMap<>();
        while (userInfo.isReadable()) {
            FirebirdUserDataType type = FirebirdUserDataType.valueOf(userInfo.readUnsignedByte());
            int length = userInfo.readUnsignedByte();
            ByteBuf data = userInfo.readSlice(length);
            if (type == FirebirdUserDataType.CNCT_SPECIFIC_DATA) {
                // specific data can be split into chunks and (i think) can be in payload in random order
                int step = data.readUnsignedByte();
                pendingData.put(step, data.toString(StandardCharsets.US_ASCII));
            } else {
                userInfoMap.put(type, data.toString(StandardCharsets.UTF_8));
            }
        }
        if (!pendingData.isEmpty()) {
            userInfoMap.put(FirebirdUserDataType.CNCT_SPECIFIC_DATA, String.join("", pendingData.values()));
        }
    }
    
    private void parseProtocols(final ByteBuf protocolBuf) {
        for (int i = 0; i < protocolsCount; i++) {
            userProtocols.add(new FirebirdProtocol(protocolBuf));
        }
    }
    
    public String getUsername() {
        return userInfoMap.get(FirebirdUserDataType.CNCT_USER);
    }
    
    public String getHost() {
        return userInfoMap.get(FirebirdUserDataType.CNCT_HOST);
    }
    
    public String getLogin() {
        return userInfoMap.get(FirebirdUserDataType.CNCT_LOGIN);
    }
    
    public String getAuthData() {
        return userInfoMap.get(FirebirdUserDataType.CNCT_SPECIFIC_DATA);
    }
    
    public String getPluginName() {
        return userInfoMap.get(FirebirdUserDataType.CNCT_PLUGIN_NAME);
    }
    
    public FirebirdAuthenticationMethod getPlugin() {
        return FirebirdAuthenticationMethod.valueOf(getPluginName().toUpperCase());
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
}
