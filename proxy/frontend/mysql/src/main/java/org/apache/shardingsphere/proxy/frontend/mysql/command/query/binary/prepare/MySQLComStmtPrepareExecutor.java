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
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.dialect.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIDGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private final MySQLComStmtPreparePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        failedIfContainsMultiStatements();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(DatabaseTypeFactory.getInstance("MySQL").getType()).parse(packet.getSql(), true);
        if (!MySQLComStmtPrepareChecker.isStatementAllowed(sqlStatement)) {
            throw new UnsupportedPreparedStatementException();
        }
        int statementId = MySQLStatementIDGenerator.getInstance().nextStatementId(connectionSession.getConnectionId());
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(),
                sqlStatement, connectionSession.getDefaultDatabaseName());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new MySQLServerPreparedStatement(packet.getSql(), sqlStatementContext));
        return createPackets(statementId, sqlStatementContext);
    }
    
    private void failedIfContainsMultiStatements() {
        // TODO Multi statements should be identified by SQL Parser instead of checking if sql contains ";".
        if (connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)
                && MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON == connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()
                && packet.getSql().contains(";")) {
            throw new UnsupportedPreparedStatementException();
        }
    }
    
    private Collection<DatabasePacket<?>> createPackets(final int statementId, final SQLStatementContext<?> sqlStatementContext) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        List<Projection> projections = getProjections(sqlStatementContext);
        int parameterCount = sqlStatementContext.getSqlStatement().getParameterCount();
        result.add(new MySQLComStmtPrepareOKPacket(statementId, projections.size(), parameterCount, 0));
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        int statusFlags = ServerStatusFlagCalculator.calculateFor(connectionSession);
        if (parameterCount > 0) {
            result.addAll(createParameterColumnDefinition41Packets(parameterCount, characterSet));
            result.add(new MySQLEofPacket(statusFlags));
        }
        if (!projections.isEmpty()) {
            result.addAll(createProjectionColumnDefinition41Packets((SelectStatementContext) sqlStatementContext, characterSet));
            result.add(new MySQLEofPacket(statusFlags));
        }
        return result;
    }
    
    private List<Projection> getProjections(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext ? ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections() : Collections.emptyList();
    }
    
    private Collection<DatabasePacket<?>> createParameterColumnDefinition41Packets(final int parameterCount, final int characterSet) {
        Collection<DatabasePacket<?>> result = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            result.add(new MySQLColumnDefinition41Packet(characterSet, "", "", "", "?", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        return result;
    }
    
    private Collection<DatabasePacket<?>> createProjectionColumnDefinition41Packets(final SelectStatementContext selectStatementContext, final int characterSet) {
        Collection<Projection> projections = selectStatementContext.getProjectionsContext().getExpandProjections();
        String databaseName = selectStatementContext.getTablesContext().getDatabaseName().orElseGet(connectionSession::getDefaultDatabaseName);
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        ShardingSphereSchema schema = selectStatementContext.getTablesContext().getSchemaName()
                .map(database::getSchema).orElseGet(() -> database.getSchema(DatabaseTypeEngine.getDefaultSchemaName(selectStatementContext.getDatabaseType(), database.getName())));
        Map<String, String> columnToTableMap = selectStatementContext.getTablesContext()
                .findTableNamesByColumnProjection(projections.stream().filter(each -> each instanceof ColumnProjection).map(each -> (ColumnProjection) each).collect(Collectors.toList()), schema);
        Collection<DatabasePacket<?>> result = new ArrayList<>(projections.size());
        for (Projection each : projections) {
            // TODO Calculate column definition flag for other projection types
            if (each instanceof ColumnProjection) {
                result.add(Optional.ofNullable(columnToTableMap.get(each.getExpression())).map(schema::getTable).map(table -> table.getColumns().get(((ColumnProjection) each).getName()))
                        .map(column -> {
                            MySQLBinaryColumnType columnType = MySQLBinaryColumnType.valueOfJDBCType(column.getDataType());
                            return new MySQLColumnDefinition41Packet(characterSet, calculateColumnDefinitionFlag(column), "", "", "", "", "", 0, columnType, 0, false);
                        })
                        .orElseGet(() -> new MySQLColumnDefinition41Packet(characterSet, "", "", "", "", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false)));
            } else {
                result.add(new MySQLColumnDefinition41Packet(characterSet, "", "", "", "", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
            }
        }
        return result;
    }
    
    private int calculateColumnDefinitionFlag(final ShardingSphereColumn column) {
        int result = 0;
        result |= column.isPrimaryKey() ? MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue() : 0;
        result |= column.isUnsigned() ? MySQLColumnDefinitionFlag.UNSIGNED.getValue() : 0;
        return result;
    }
}
