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

package org.apache.shardingsphere.proxy.frontend.reactive.mysql.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecutorFactory;
import org.apache.shardingsphere.proxy.frontend.reactive.command.executor.ReactiveCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.reactive.mysql.command.query.binary.execute.ReactiveMySQLComStmtExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.reactive.mysql.command.query.text.fieldlist.ReactiveMySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.proxy.frontend.reactive.mysql.command.query.text.query.ReactiveMySQLComQueryPacketExecutor;
import org.apache.shardingsphere.proxy.frontend.reactive.wrap.WrappedReactiveCommandExecutor;

import java.sql.SQLException;

/**
 * Reactive command executor factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ReactiveMySQLCommandExecutorFactory {
    
    /**
     * Create new instance of reactive packet executor.
     *
     * @param commandPacketType command packet type for MySQL
     * @param commandPacket command packet for MySQL
     * @param connectionSession connection session
     * @return command executor
     */
    @SneakyThrows(SQLException.class)
    public static ReactiveCommandExecutor newInstance(final MySQLCommandPacketType commandPacketType, final CommandPacket commandPacket, final ConnectionSession connectionSession) {
        log.debug("Execute packet type: {}, value: {}", commandPacketType, commandPacket);
        switch (commandPacketType) {
            case COM_FIELD_LIST:
                return new ReactiveMySQLComFieldListPacketExecutor((MySQLComFieldListPacket) commandPacket, connectionSession);
            case COM_QUERY:
                return new ReactiveMySQLComQueryPacketExecutor((MySQLComQueryPacket) commandPacket, connectionSession);
            case COM_STMT_EXECUTE:
                return new ReactiveMySQLComStmtExecuteExecutor((MySQLComStmtExecutePacket) commandPacket, connectionSession);
            default:
                return new WrappedReactiveCommandExecutor(MySQLCommandExecutorFactory.newInstance(commandPacketType, commandPacket, connectionSession));
        }
    }
}
