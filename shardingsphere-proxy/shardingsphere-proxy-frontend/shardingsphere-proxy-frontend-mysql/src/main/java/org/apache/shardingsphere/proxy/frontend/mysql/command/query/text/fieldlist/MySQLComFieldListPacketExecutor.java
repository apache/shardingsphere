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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.fieldlist;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.MySQLErrPacketFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * COM_FIELD_LIST packet executor for MySQL.
 */
public final class MySQLComFieldListPacketExecutor implements CommandExecutor {
    
    private static final String SQL = "SHOW COLUMNS FROM %s FROM %s";
    
    private final MySQLComFieldListPacket packet;
    
    private final String schemaName;
    
    private final DatabaseCommunicationEngine databaseCommunicationEngine;
    
    private int currentSequenceId;
    
    public MySQLComFieldListPacketExecutor(final MySQLComFieldListPacket packet, final BackendConnection backendConnection) {
        this.packet = packet;
        schemaName = backendConnection.getSchema();
        String sql = String.format(SQL, packet.getTable(), schemaName);
        SQLStatement sqlStatement = ProxyContext.getInstance().getSchema(backendConnection.getSchema()).getRuntimeContext().getSqlParserEngine().parse(sql, false);
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newTextProtocolInstance(sqlStatement, sql, backendConnection);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        BackendResponse backendResponse = databaseCommunicationEngine.execute();
        return backendResponse instanceof ErrorResponse ? createErrorPackets((ErrorResponse) backendResponse) : createColumnDefinition41Packets();
    }
    
    private List<DatabasePacket<?>> createErrorPackets(final ErrorResponse backendResponse) {
        return Collections.singletonList(MySQLErrPacketFactory.newInstance(1, backendResponse.getCause()));
    }
    
    private Collection<DatabasePacket<?>> createColumnDefinition41Packets() throws SQLException {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        while (databaseCommunicationEngine.next()) {
            String columnName = databaseCommunicationEngine.getQueryData().getData().get(0).toString();
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, schemaName, packet.getTable(), packet.getTable(), columnName, columnName, 100, MySQLColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
}
