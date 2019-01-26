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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake;

import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;

/**
 * Authentication Ok message.
 *
 * @author zhangyonglun
 */
public final class AuthenticationOk implements PostgreSQLPacket {
    
    private final char messageType = PostgreSQLCommandPacketType.AUTHENTICATION_OK.getValue();
    
    private int messageLength = 8;
    
    private final int success;
    
    public AuthenticationOk(final boolean isSuccess) {
        success = isSuccess ? 0 : 1;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt1(messageType);
        payload.writeInt4(messageLength);
        payload.writeInt4(success);
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
