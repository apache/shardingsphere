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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

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
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final MySQLComFieldListPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseProxyConnector databaseProxyConnector;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        String currentDatabaseName = connectionSession.getCurrentDatabaseName();
        String sql = String.format(SQL, packet.getTable(), currentDatabaseName);
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(sql, false);
        HintValueContext hintValueContext = SQLHintUtils.extractHint(sql);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaDataContexts.getMetaData(), currentDatabaseName, hintValueContext).bind(sqlStatement);
        ProxyDatabaseConnectionManager databaseConnectionManager = connectionSession.getDatabaseConnectionManager();
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext, connectionSession.getConnectionContext(), metaDataContexts.getMetaData());
        databaseProxyConnector = DatabaseProxyConnectorFactory.newInstance(queryContext, databaseConnectionManager, false);
        databaseProxyConnector.execute();
        return createColumnDefinition41Packets(currentDatabaseName);
    }
    
    private Collection<DatabasePacket> createColumnDefinition41Packets(final String databaseName) throws SQLException {
        Collection<DatabasePacket> result = new LinkedList<>();
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        while (databaseProxyConnector.next()) {
            String columnName = databaseProxyConnector.getRowData().getCells().iterator().next().getData().toString();
            result.add(new MySQLColumnDefinition41Packet(
                    characterSet, databaseName, packet.getTable(), packet.getTable(), columnName, columnName, 100, MySQLBinaryColumnType.VARCHAR, 0, true));
        }
        result.add(new MySQLEofPacket(ServerStatusFlagCalculator.calculateFor(connectionSession, true)));
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        if (null != databaseProxyConnector) {
            databaseProxyConnector.close();
        }
    }
}
