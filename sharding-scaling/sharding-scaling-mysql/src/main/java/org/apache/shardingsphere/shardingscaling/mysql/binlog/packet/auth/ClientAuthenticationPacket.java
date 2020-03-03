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

import org.apache.shardingsphere.shardingscaling.mysql.binlog.MySQLPasswordEncryptor;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.CapabilityFlags;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;

import com.google.common.base.Strings;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * MySQL client authentication packet.
 */
@Setter
public final class ClientAuthenticationPacket extends AbstractPacket {
    
    @Setter(AccessLevel.NONE)
    private int clientCapability = CapabilityFlags.CLIENT_LONG_PASSWORD | CapabilityFlags.CLIENT_LONG_FLAG
            | CapabilityFlags.CLIENT_PROTOCOL_41 | CapabilityFlags.CLIENT_INTERACTIVE
            | CapabilityFlags.CLIENT_TRANSACTIONS | CapabilityFlags.CLIENT_SECURE_CONNECTION
            | CapabilityFlags.CLIENT_MULTI_STATEMENTS | CapabilityFlags.CLIENT_PLUGIN_AUTH;
    
    private String username;
    
    private String password;
    
    private byte charsetNumber;
    
    private String databaseName;
    
    private int serverCapabilities;
    
    private byte[] authPluginData;
    
    private String authPluginName;
    
    /**
     * Set database name.
     *
     * @param databaseName database name
     */
    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        if (databaseName != null) {
            this.clientCapability |= CapabilityFlags.CLIENT_CONNECT_WITH_DB;
        }
    }
    
    /**
     * Set auth plugin name.
     *
     * @param authPluginName auth plugin name
     */
    public void setAuthPluginName(final String authPluginName) {
        this.authPluginName = authPluginName;
        if (authPluginName != null) {
            this.clientCapability |= CapabilityFlags.CLIENT_PLUGIN_AUTH;
        }
    }
    
    @Override
    @SneakyThrows
    public ByteBuf toByteBuf() {
        ByteBuf result = ByteBufAllocator.DEFAULT.heapBuffer();
        DataTypesCodec.writeInt4LE(clientCapability, result);
        DataTypesCodec.writeInt4LE(1 << 24, result);
        DataTypesCodec.writeByte(charsetNumber, result);
        DataTypesCodec.writeBytes(new byte[23], result);
        DataTypesCodec.writeBytes(username.getBytes(), result);
        DataTypesCodec.writeByte((byte) 0x00, result);
        if (Strings.isNullOrEmpty(password)) {
            DataTypesCodec.writeByte((byte) 0x00, result);
        } else {
            byte[] encryptedPassword = MySQLPasswordEncryptor.encryptWithMySQL41(password.getBytes(), authPluginData);
            DataTypesCodec.writeLengthCodedBinary(encryptedPassword, result);
        }
        if (null != databaseName) {
            DataTypesCodec.writeNulTerminatedString(databaseName, result);
        }
        if (null != authPluginName) {
            DataTypesCodec.writeNulTerminatedString(authPluginName, result);
        }
        return result;
    }
}
