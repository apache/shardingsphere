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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * COM_FIELD_LIST packet executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComFieldListPacketExecutor implements CommandExecutor {
    
    private static final String SQL = "SHOW COLUMNS FROM %s FROM %s";
    
    private final MySQLComFieldListPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private JDBCDatabaseCommunicationEngine databaseCommunicationEngine;
    
    private int currentSequenceId;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        String databaseName = connectionSession.getDefaultDatabaseName();
        String sql = String.format(SQL, packet.getTable(), databaseName);
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(DatabaseTypeFactory.getInstance("MySQL"))).parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData().getDatabases(), sqlStatement, databaseName);
        JDBCBackendConnection backendConnection = (JDBCBackendConnection) connectionSession.getBackendConnection();
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList());
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(queryContext, backendConnection, false);
        databaseCommunicationEngine.execute();
        return createColumnDefinition41Packets(databaseName);
    }
    
    private Collection<DatabasePacket<?>> createColumnDefinition41Packets(final String databaseName) throws SQLException {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        while (databaseCommunicationEngine.next()) {
            String columnName = databaseCommunicationEngine.getRowData().getCells().iterator().next().getData().toString();
            result.add(new MySQLColumnDefinition41Packet(
                    ++currentSequenceId, characterSet, databaseName, packet.getTable(), packet.getTable(), columnName, columnName, 100, MySQLBinaryColumnType.MYSQL_TYPE_VARCHAR, 0, true));
        }
        result.add(new MySQLEofPacket(++currentSequenceId, ServerStatusFlagCalculator.calculateFor(connectionSession)));
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        databaseCommunicationEngine.close();
    }
}
