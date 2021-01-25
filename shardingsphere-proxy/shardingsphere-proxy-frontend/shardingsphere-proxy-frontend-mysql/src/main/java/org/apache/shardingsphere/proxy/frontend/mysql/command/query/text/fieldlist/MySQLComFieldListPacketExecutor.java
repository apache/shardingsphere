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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

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
        schemaName = backendConnection.getSchemaName();
        String sql = String.format(SQL, packet.getTable(), schemaName);
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType()));
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newTextProtocolInstance(sqlStatement, sql, backendConnection);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        databaseCommunicationEngine.execute();
        return createColumnDefinition41Packets();
    }
    
    private Collection<DatabasePacket<?>> createColumnDefinition41Packets() throws SQLException {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        while (databaseCommunicationEngine.next()) {
            String columnName = databaseCommunicationEngine.getQueryResponseRow().getCells().iterator().next().getData().toString();
            result.add(new MySQLColumnDefinition41Packet(
                    ++currentSequenceId, schemaName, packet.getTable(), packet.getTable(), columnName, columnName, 100, MySQLBinaryColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
}
