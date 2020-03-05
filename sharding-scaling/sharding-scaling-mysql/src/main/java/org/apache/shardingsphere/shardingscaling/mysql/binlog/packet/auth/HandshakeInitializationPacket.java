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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.auth;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.CapabilityFlags;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * MySQL handshake initialization packet.
 *
 * <p>
 *     https://github.com/mysql/mysql-server/blob/5.7/sql/auth/sql_authentication.cc
 *     Bytes       Content
 *     -----       ----
 *     1           protocol version (always 10)
 *     n           server version string, \0-terminated
 *     4           thread id
 *     8           first 8 bytes of the plugin provided data (scramble)
 *     1           \0 byte, terminating the first part of a scramble
 *     2           server capabilities (two lower bytes)
 *     1           server character set
 *     2           server status
 *     2           server capabilities (two upper bytes)
 *     1           length of the scramble
 *     10          reserved, always 0
 *     n           rest of the plugin provided data (at least 12 bytes)
 *     1           \0 byte, terminating the second part of a scramble
 * </p>
 */
@Setter
@Getter
public final class HandshakeInitializationPacket extends AbstractPacket {
    
    private short protocolVersion = 0x0a;
    
    private String serverVersion;
    
    private long threadId;
    
    private byte[] authPluginDataPart1;
    
    private int serverCapabilities;
    
    private short serverCharsetSet;
    
    private int serverStatus;
    
    private int serverCapabilities2;
    
    private byte[] authPluginDataPart2;
    
    private String authPluginName;
    
    /**
     * There are some different between implement of handshake initialization packet and document.
     * In source code of 5.7 version, authPluginDataPart2 should be at least 12 bytes,
     * and then follow a nul byte.
     * But in document, authPluginDataPart2 is at least 13 bytes, and not nul byte.
     * From test, the 13th byte is nul byte and should be excluded from authPluginDataPart2.
     *
     * @param data buffer
     */
    @Override
    public void fromByteBuf(final ByteBuf data) {
        protocolVersion = DataTypesCodec.readUnsignedInt1(data);
        serverVersion = DataTypesCodec.readNulTerminatedString(data);
        threadId = DataTypesCodec.readUnsignedInt4LE(data);
        authPluginDataPart1 = DataTypesCodec.readBytes(8, data);
        DataTypesCodec.readNul(data);
        serverCapabilities = DataTypesCodec.readUnsignedInt2LE(data);
        if (data.isReadable()) {
            serverCharsetSet = DataTypesCodec.readUnsignedInt1(data);
            serverStatus = DataTypesCodec.readUnsignedInt2LE(data);
            serverCapabilities2 = DataTypesCodec.readUnsignedInt2LE(data);
            int capabilities = (serverCapabilities2 << 16) | serverCapabilities;
            int authPluginDataLength = DataTypesCodec.readUnsignedInt1(data);
            DataTypesCodec.readBytes(10, data);
            if ((capabilities & CapabilityFlags.CLIENT_SECURE_CONNECTION) != 0) {
                authPluginDataPart2 = DataTypesCodec.readBytes(Math.max(12, authPluginDataLength - 8 - 1), data);
                DataTypesCodec.readNul(data);
            }
            if ((capabilities & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0) {
                authPluginName = DataTypesCodec.readNulTerminatedString(data);
            }
        }
    }
}
