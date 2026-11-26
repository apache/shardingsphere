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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Firebird array slice response packet.
 */
public final class FirebirdSliceResponsePacket extends FirebirdPacket {
    
    private final int sliceLength;
    
    private final byte[] data;
    
    /**
     * Construct a response using the native byte length and the XDR data encoded according to the request's SDL.
     *
     * @param sliceLength native length of the returned slice
     * @param data XDR-encoded slice elements including their padding
     */
    public FirebirdSliceResponsePacket(final int sliceLength, final byte[] data) {
        this.sliceLength = sliceLength;
        this.data = data;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.SLICE.getValue());
        payload.writeInt4(sliceLength);
        payload.writeInt4(sliceLength);
        payload.writeBytes(data);
    }
}
