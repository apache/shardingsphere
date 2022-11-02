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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.dialect.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIDGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private final MySQLComStmtPreparePacket packet;
    
    private final ConnectionSession connectionSession;
    
    private int currentSequenceId;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        failedIfContainsMultiStatements();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(
                DatabaseTypeEngine.getTrunkDatabaseTypeName(metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getProtocolType())).parse(packet.getSql(), true);
        if (!MySQLComStmtPrepareChecker.isStatementAllowed(sqlStatement)) {
            throw new UnsupportedPreparedStatementException();
        }
        int projectionCount = getProjectionCount(sqlStatement);
        int statementId = MySQLStatementIDGenerator.getInstance().nextStatementId(connectionSession.getConnectionId());
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases(),
                sqlStatement, connectionSession.getDefaultDatabaseName());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new MySQLServerPreparedStatement(packet.getSql(), sqlStatementContext));
        return createPackets(statementId, projectionCount, sqlStatement.getParameterCount());
    }
    
    private void failedIfContainsMultiStatements() {
        // TODO Multi statements should be identified by SQL Parser instead of checking if sql contains ";".
        if (connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)
                && MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON == connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()
                && packet.getSql().contains(";")) {
            throw new UnsupportedPreparedStatementException();
        }
    }
    
    private int getProjectionCount(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
            String databaseName = connectionSession.getDatabaseName();
            SelectStatementContext sqlStatementContext = (SelectStatementContext) SQLStatementContextFactory.newInstance(databases, sqlStatement, databaseName);
            return sqlStatementContext.getProjectionsContext().getExpandProjections().size();
        }
        return 0;
    }
    
    private Collection<DatabasePacket<?>> createPackets(final int statementId, final int projectionCount, final int parameterCount) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        result.add(new MySQLComStmtPrepareOKPacket(++currentSequenceId, statementId, projectionCount, parameterCount, 0));
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        int statusFlags = ServerStatusFlagCalculator.calculateFor(connectionSession);
        if (parameterCount > 0) {
            result.addAll(createParameterColumnDefinition41Packets(parameterCount, characterSet, statusFlags));
        }
        if (projectionCount > 0) {
            result.addAll(createProjectionColumnDefinition41Packets(projectionCount, characterSet, statusFlags));
        }
        return result;
    }
    
    private Collection<DatabasePacket<?>> createParameterColumnDefinition41Packets(final int parameterCount, final int characterSet, final int statusFlags) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (int i = 0; i < parameterCount; i++) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, characterSet, "", "", "", "?", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        result.add(new MySQLEofPacket(++currentSequenceId, statusFlags));
        return result;
    }
    
    private Collection<DatabasePacket<?>> createProjectionColumnDefinition41Packets(final int projectionCount, final int characterSet, final int statusFlags) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (int i = 0; i < projectionCount; i++) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, characterSet, "", "", "", "", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        result.add(new MySQLEofPacket(++currentSequenceId, statusFlags));
        return result;
    }
}
