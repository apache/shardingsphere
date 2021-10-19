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

package org.apache.shardingsphere.proxy.frontend.opengauss.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.binary.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.binary.bind.OpenGaussComBatchBindExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLCommandExecutorFactory;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;

import java.sql.SQLException;

/**
 * Command executor factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class OpenGaussCommandExecutorFactory {
    
    /**
     * Create new instance of command executor.
     *
     * @param commandPacketType command packet type for PostgreSQL/openGauss
     * @param commandPacket command packet for PostgreSQL/openGauss
     * @param backendConnection backend connection
     * @param connectionContext PostgreSQL connection context
     * @return command executor
     * @throws SQLException SQL exception
     */
    public static CommandExecutor newInstance(final CommandPacketType commandPacketType, final CommandPacket commandPacket,
                                              final BackendConnection backendConnection, final PostgreSQLConnectionContext connectionContext) throws SQLException {
        return commandPacketType == OpenGaussCommandPacketType.BATCH_BIND_COMMAND ? new OpenGaussComBatchBindExecutor((OpenGaussComBatchBindPacket) commandPacket, backendConnection)
                : PostgreSQLCommandExecutorFactory.newInstance((PostgreSQLCommandPacketType) commandPacketType, (PostgreSQLCommandPacket) commandPacket, backendConnection, connectionContext);
    }
}
