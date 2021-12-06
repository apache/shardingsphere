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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 */
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private final MySQLComStmtPreparePacket packet;
    
    private final BackendConnection backendConnection;
    
    private final int characterSet;
    
    private int currentSequenceId;
    
    public MySQLComStmtPrepareExecutor(final MySQLComStmtPreparePacket packet, final BackendConnection backendConnection) {
        this.packet = packet;
        this.backendConnection = backendConnection;
        characterSet = backendConnection.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                metaDataContexts.getMetaData(backendConnection.getSchemaName()).getResource().getDatabaseType()),
                metaDataContexts.getGlobalRuleMetaData().findSingleRule(SQLParserRule.class).orElse(null));
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(packet.getSql(), true);
        if (!MySQLComStmtPrepareChecker.isStatementAllowed(sqlStatement)) {
            throw new UnsupportedPreparedStatementException();
        }
        int parameterCount = sqlStatement.getParameterCount();
        int projectionCount = getProjectionCount(sqlStatement);
        int statementId = MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(backendConnection.getConnectionId()).prepareStatement(packet.getSql(), parameterCount);
        return createPackets(statementId, projectionCount, parameterCount);
    }
    
    private int getProjectionCount(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            Map<String, ShardingSphereMetaData> metaDataMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap();
            String schemaName = backendConnection.getSchemaName();
            SelectStatementContext sqlStatementContext = (SelectStatementContext) SQLStatementContextFactory.newInstance(metaDataMap, Collections.emptyList(), sqlStatement, schemaName);
            return sqlStatementContext.getProjectionsContext().getExpandProjections().size();
        }
        return 0;
    }

    private Collection<DatabasePacket<?>> createPackets(final int statementId, final int projectionCount, final int parameterCount) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        result.add(new MySQLComStmtPrepareOKPacket(++currentSequenceId, statementId, projectionCount, parameterCount, 0));
        if (parameterCount > 0) {
            result.addAll(createParameterColumnDefinition41Packets(parameterCount));
        }
        if (projectionCount > 0) {
            result.addAll(createProjectionColumnDefinition41Packets(projectionCount));
        }
        return result;
    }
    
    private Collection<DatabasePacket<?>> createParameterColumnDefinition41Packets(final int parameterCount) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (int i = 0; i < parameterCount; i++) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, characterSet, "", "", "", "?", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
    
    private Collection<DatabasePacket<?>> createProjectionColumnDefinition41Packets(final int projectionCount) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (int i = 0; i < projectionCount; i++) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, characterSet, "", "", "", "", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
}
