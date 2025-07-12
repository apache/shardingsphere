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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Generic response data packet for Firebird.
 */
@Getter
@NoArgsConstructor
public final class FirebirdGenericResponsePacket extends FirebirdPacket {
    
    private int handle;
    
    private long id;
    
    private FirebirdPacket data;
    
    private byte[] statusVector = getEmptyStatusVector();
    
    /**
     * Set handle value.
     *
     * @param objectHandle handle value to set
     * @return this instance with updated handle
     */
    public FirebirdGenericResponsePacket setHandle(final int objectHandle) {
        handle = objectHandle;
        return this;
    }
    
    /**
     * Set ID value.
     *
     * @param objectId ID value to set
     * @return this instance with updated ID
     */
    public FirebirdGenericResponsePacket setId(final int objectId) {
        id = objectId;
        return this;
    }
    
    /**
     * Set return data packet.
     *
     * @param packet data packet to set
     * @return this instance with updated data
     */
    public FirebirdGenericResponsePacket setData(final FirebirdPacket packet) {
        data = packet;
        return this;
    }
    
    /**
     * Set status vector.
     *
     * @param buffer status vector to set
     * @return this instance with updated status vector
     */
    public FirebirdGenericResponsePacket setStatusVector(final byte[] buffer) {
        statusVector = buffer;
        return this;
    }
    
    /**
     * Set error status vector based on the given SQL exception.
     *
     * @param ex SQL exception
     * @return this instance with updated status vector
     */
    public FirebirdGenericResponsePacket setErrorStatusVector(final SQLException ex) {
        int gdsCode = ex.getErrorCode();
        boolean isFirebirdCode = gdsCode >= 335544321 && gdsCode != 335544382;
        if (!isFirebirdCode) {
            gdsCode = 335544382;
        }
        
        String rawMessage = ex.getMessage();
        int idx = rawMessage.indexOf(';');
        String message = idx >= 0 ? rawMessage.substring(idx + 1).trim() : rawMessage;
        int stateIdx = message.indexOf(" [SQLState:");
        if (stateIdx >= 0) {
            message = message.substring(0, stateIdx).trim();
        }
        
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        buf.writeInt(gdsCode);
        buf.writeInt(2);
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(msgBytes.length);
        buf.writeBytes(msgBytes);
        int pad = (4 - (msgBytes.length % 4)) % 4;
        if (pad > 0) {
            buf.writeZero(pad);
        }
        buf.writeInt(0);
        
        byte[] vec = new byte[buf.readableBytes()];
        buf.readBytes(vec);
        buf.release();
        this.statusVector = vec;
        return this;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.RESPONSE.getValue());
        payload.writeInt4(handle);
        payload.writeInt8(id);
        if (data != null) {
            payload.getByteBuf().writeZero(4);
            int index = payload.getByteBuf().readableBytes();
            data.write(payload);
            int length = payload.getByteBuf().readableBytes() - index;
            payload.getByteBuf().setInt(index - 4, length);
            payload.getByteBuf().writeBytes(new byte[(4 - length) & 3]);
        } else {
            payload.writeBuffer(new byte[0]);
        }
        payload.writeBytes(statusVector);
    }
    
    private static byte[] getEmptyStatusVector() {
        byte[] statusVector = new byte[4];
        Arrays.fill(statusVector, (byte) 0);
        return statusVector;
    }
    
    public static FirebirdGenericResponsePacket getPacket() {
        return new FirebirdGenericResponsePacket();
    }
    
    /**
     * Extract error code from status vector.
     *
     * @return error code
     */
    public int getErrorCode() {
        if (statusVector == null || statusVector.length < 8) {
            return -1;
        }
        ByteBuf buf = Unpooled.wrappedBuffer(statusVector);
        buf.readInt();
        return buf.readInt();
    }
    
    /**
     * Extract error message from status vector.
     *
     * @return error message
     */
    public String getErrorMessage() {
        if (statusVector == null || statusVector.length < 16) {
            return "";
        }
        ByteBuf buf = Unpooled.wrappedBuffer(statusVector);
        buf.readInt();
        buf.readInt();
        buf.readInt();
        int msgLen = buf.readInt();
        byte[] msgBytes = new byte[msgLen];
        buf.readBytes(msgBytes);
        return new String(msgBytes, StandardCharsets.UTF_8);
    }
    
}
