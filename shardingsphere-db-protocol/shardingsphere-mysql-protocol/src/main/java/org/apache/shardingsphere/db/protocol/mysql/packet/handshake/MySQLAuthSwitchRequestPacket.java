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

package org.apache.shardingsphere.db.protocol.mysql.packet.handshake;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.Arrays;

/**
 * MySQL authentication switch request packet.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::AuthSwitchRequest">AuthSwitchRequest</a>
 */
@RequiredArgsConstructor
public final class MySQLAuthSwitchRequestPacket implements MySQLPacket {
    
    /**
     * Header of MySQL auth switch request packet.
     */
    public static final int HEADER = 0xfe;
    
    @Getter
    private final int sequenceId;
    
    @Getter
    private final String authPluginName;
    
    @Getter
    private final MySQLAuthPluginData authPluginData;
    
    public MySQLAuthSwitchRequestPacket(final MySQLPacketPayload payload) {
        sequenceId = payload.readInt1();
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL auth switch request packet must be `0xfe`.");
        authPluginName = payload.readStringNul();
        String strAuthPluginData = payload.readStringNul();
        authPluginData = new MySQLAuthPluginData(Arrays.copyOfRange(strAuthPluginData.getBytes(), 0, 8),
                Arrays.copyOfRange(strAuthPluginData.getBytes(), 8, 20));
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeStringNul(authPluginName);
        payload.writeStringNul(new String(authPluginData.getAuthenticationPluginData()));
    }
}
