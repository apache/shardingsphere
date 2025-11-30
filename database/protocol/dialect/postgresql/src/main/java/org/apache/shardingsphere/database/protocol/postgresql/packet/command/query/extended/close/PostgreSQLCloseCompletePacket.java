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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close;

import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Close complete packet for PostgreSQL.
 */
public final class PostgreSQLCloseCompletePacket extends PostgreSQLPacket {
    
    private static final byte[] VALUE = {(byte) PostgreSQLMessagePacketType.CLOSE_COMPLETE.getValue(), 0, 0, 0, 4};
    
    private static final PostgreSQLCloseCompletePacket INSTANCE = new PostgreSQLCloseCompletePacket();
    
    /**
     * Get instance of {@link PostgreSQLCloseCompletePacket}.
     *
     * @return instance of {@link PostgreSQLCloseCompletePacket}
     */
    public static PostgreSQLCloseCompletePacket getInstance() {
        return INSTANCE;
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        payload.getByteBuf().writeBytes(VALUE);
    }
}
