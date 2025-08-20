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
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdDatabaseParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Attach packet for Firebird.
 */
@Getter
public final class FirebirdAttachPacket extends FirebirdPacket {
    
    private final int id;
    
    private final String database;
    
    private final FirebirdParameterBuffer dpb = FirebirdDatabaseParameterBufferType.createBuffer();
    
    public FirebirdAttachPacket(final FirebirdPacketPayload payload) {
        id = payload.readInt4();
        database = payload.readString();
        dpb.parseBuffer(payload.readBuffer());
    }
    
    public String getEncoding() {
        return dpb.getValue(FirebirdDatabaseParameterBufferType.LC_CTYPE);
    }
    
    public String getAuthData() {
        return dpb.getValue(FirebirdDatabaseParameterBufferType.SPECIFIC_AUTH_DATA);
    }
    
    public String getUsername() {
        return dpb.getValue(FirebirdDatabaseParameterBufferType.USER_NAME);
    }
    
    public String getEncPassword() {
        return dpb.getValue(FirebirdDatabaseParameterBufferType.PASSWORD_ENC);
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        
    }
}
