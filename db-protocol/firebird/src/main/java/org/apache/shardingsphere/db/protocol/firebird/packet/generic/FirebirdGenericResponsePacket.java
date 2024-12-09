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

package org.apache.shardingsphere.db.protocol.firebird.packet.generic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.Arrays;

/**
 * Accept data packet for Firebird.
 */
@Getter
@NoArgsConstructor
public final class FirebirdGenericResponsePacket extends FirebirdPacket {

    private int handle = 0;
    private long id = 0;
    private byte[] data = new byte[0];
    private byte[] statusVector = getEmptyStatusVector();

    public FirebirdGenericResponsePacket setHandle(final int objectHandle) {
        handle = objectHandle;
        return this;
    }

    public FirebirdGenericResponsePacket setId(final int objectId) {
        id = objectId;
        return this;
    }

    public FirebirdGenericResponsePacket setData(final byte[] buffer) {
        data = buffer;
        return this;
    }

    public FirebirdGenericResponsePacket setStatusVector(final byte[] buffer) {
        statusVector = buffer;
        return this;
    }

    @Override
    protected void write(FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.OP_RESPONSE.getValue());
        payload.writeInt4(handle);
        payload.writeInt8(id);
        payload.writeBuffer(data);
        payload.writeBytes(statusVector);
    }

    static private byte[] getEmptyStatusVector() {
        byte[] statusVector = new byte[4];
        Arrays.fill(statusVector, (byte) 0);
        return statusVector;
    }

    static public FirebirdGenericResponsePacket getPacket() {
        return new FirebirdGenericResponsePacket();
    }
}