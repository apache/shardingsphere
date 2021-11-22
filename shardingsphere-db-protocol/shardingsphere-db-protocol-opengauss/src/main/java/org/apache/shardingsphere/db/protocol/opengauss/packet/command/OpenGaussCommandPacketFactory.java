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

package org.apache.shardingsphere.db.protocol.opengauss.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Command packet factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussCommandPacketFactory {
    
    /**
     * Create new instance of command packet.
     *
     * @param commandPacketType command packet type for PostgreSQL/openGauss
     * @param payload packet payload for PostgreSQL/openGauss
     * @param connectionId connection ID
     * @return command packet for PostgreSQL/openGauss
     */
    public static CommandPacket newInstance(final CommandPacketType commandPacketType, final PostgreSQLPacketPayload payload, final int connectionId) {
        return commandPacketType == OpenGaussCommandPacketType.BATCH_BIND_COMMAND ? new OpenGaussComBatchBindPacket(payload, connectionId)
                : PostgreSQLCommandPacketFactory.newInstance((PostgreSQLCommandPacketType) commandPacketType, payload, connectionId);
    }
}
