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

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.sql.SQLException;

/**
 * Generic response data packet for Firebird.
 */
@Getter
public final class FirebirdGenericResponsePacket extends FirebirdPacket {
    
    private int handle;
    
    private long id;
    
    private FirebirdPacket data;
    
    private FirebirdStatusVector statusVector;
    
    private boolean writeZeroStatementId;
    
    /**
     * Get generic response packet.
     *
     * @return generic response packet
     */
    public static FirebirdGenericResponsePacket getPacket() {
        return new FirebirdGenericResponsePacket();
    }
    
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
     * Set ID value.
     *
     * @param objectId ID value to set
     * @return this instance with updated ID
     */
    public FirebirdGenericResponsePacket setId(final long objectId) {
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
     * Set error status vector based on the given SQL exception.
     *
     * @param ex SQL exception
     * @return this instance with updated status vector
     */
    public FirebirdGenericResponsePacket setErrorStatusVector(final SQLException ex) {
        statusVector = new FirebirdStatusVector(ex);
        return this;
    }
    
    /**
     * Extract error code from status vector.
     *
     * @return error code
     */
    public int getErrorCode() {
        return statusVector == null ? -1 : statusVector.getGdsCode();
    }
    
    /**
     * Extract error message from status vector.
     *
     * @return error message
     */
    public String getErrorMessage() {
        return statusVector == null ? "" : statusVector.getErrorMessage();
    }
    
    /**
     * Set flag to write zero statement id (handle) once.
     *
     * @param writeZeroStatementId whether to write zero statement id
     * @return this instance with updated flag
     */
    public FirebirdGenericResponsePacket setWriteZeroStatementId(final boolean writeZeroStatementId) {
        this.writeZeroStatementId = writeZeroStatementId;
        return this;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.RESPONSE.getValue());
        // TODO Replace temporary handle zeroing with proper statement handle management (e.g. statement cache).
        payload.writeInt4(writeZeroStatementId ? 0 : handle);
        writeZeroStatementId = false;
        payload.writeInt8(id);
        if (null != data) {
            payload.getByteBuf().writeZero(4);
            int index = payload.getByteBuf().readableBytes();
            data.write(payload);
            int length = payload.getByteBuf().readableBytes() - index;
            payload.getByteBuf().setInt(index - 4, length);
            payload.getByteBuf().writeBytes(new byte[(4 - length) & 3]);
        } else {
            payload.writeInt4(0);
        }
        if (null != statusVector) {
            statusVector.write(payload);
        } else {
            payload.getByteBuf().writeZero(4);
        }
    }
}
