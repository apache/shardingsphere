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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ClientAuthenticationPacketTest {
    
    @Test
    public void assertToByteBufWithoutPassword() {
        ClientAuthenticationPacket clientAuthenticationPacket = createClientAuthenticationPacket();
        ByteBuf actual = clientAuthenticationPacket.toByteBuf();
        assertThat(ByteBufUtil.hexDump(actual), is("0da6090000000001080000000000000000000000000000000000000000000000726f6f7400006d7973716c006d7973716c5f6e61746976655f70617373776f726400"));
    }
    
    @Test
    public void assertToByteBufWithPassword() {
        ClientAuthenticationPacket clientAuthenticationPacket = createClientAuthenticationPacket();
        clientAuthenticationPacket.setPassword("root");
        ByteBuf actual = clientAuthenticationPacket.toByteBuf();
        assertThat(ByteBufUtil.hexDump(actual),
            is("0da6090000000001080000000000000000000000000000000000000000000000726f6f740014bccb5cac49b1430878e1fe07dff923de69b477af6d7973716c006d7973716c5f6e61746976655f70617373776f726400"));
    }
    
    private ClientAuthenticationPacket createClientAuthenticationPacket() {
        ClientAuthenticationPacket result = new ClientAuthenticationPacket();
        result.setUsername("root");
        result.setCharsetNumber((byte) 0x08);
        result.setDatabaseName("mysql");
        result.setServerCapabilities(64);
        result.setAuthPluginData(new byte[20]);
        result.setAuthPluginName("mysql_native_password");
        return result;
    }
}
