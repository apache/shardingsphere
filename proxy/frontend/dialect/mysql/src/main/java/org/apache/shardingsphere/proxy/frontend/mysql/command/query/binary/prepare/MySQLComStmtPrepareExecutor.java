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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.mysql.exception.TooManyPlaceholdersException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private static final int MAX_PARAMETER_COUNT = 65535;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final MySQLComStmtPreparePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        failedIfContainsMultiStatements();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(packet.getSQL(), true);
        ShardingSpherePreconditions.checkState(MySQLComStmtPrepareChecker.isAllowedStatement(sqlStatement), UnsupportedPreparedStatementException::new);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(),
                connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement);
        int statementId = MySQLStatementIdGenerator.getInstance().nextStatementId(connectionSession.getConnectionId());
        MySQLServerPreparedStatement serverPreparedStatement = new MySQLServerPreparedStatement(packet.getSQL(), sqlStatementContext, packet.getHintValueContext(), new CopyOnWriteArrayList<>());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        return createPackets(sqlStatementContext, statementId, serverPreparedStatement);
    }
    
    private void failedIfContainsMultiStatements() {
        // TODO Multi statements should be identified by SQL Parser instead of checking if sql contains ";".
        if (connectionSession.getAttributeMap().hasAttr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY)
                && MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON == connectionSession.getAttributeMap().attr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY).get()
                && packet.getSQL().contains(";")) {
            throw new UnsupportedPreparedStatementException();
        }
    }
    
    private Collection<DatabasePacket> createPackets(final SQLStatementContext sqlStatementContext, final int statementId, final MySQLServerPreparedStatement serverPreparedStatement) {
        Collection<DatabasePacket> result = new LinkedList<>();
        Collection<Projection> projections = getProjections(sqlStatementContext);
        int parameterCount = sqlStatementContext.getSqlStatement().getParameterCount();
        ShardingSpherePreconditions.checkState(parameterCount <= MAX_PARAMETER_COUNT, TooManyPlaceholdersException::new);
        result.add(new MySQLComStmtPrepareOKPacket(statementId, projections.size(), parameterCount, 0));
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        int statusFlags = ServerStatusFlagCalculator.calculateFor(connectionSession, true);
        if (parameterCount > 0) {
            result.addAll(createParameterColumnDefinition41Packets(sqlStatementContext, characterSet, serverPreparedStatement));
            result.add(new MySQLEofPacket(statusFlags));
        }
        if (!projections.isEmpty() && sqlStatementContext instanceof SelectStatementContext) {
            result.addAll(createProjectionColumnDefinition41Packets((SelectStatementContext) sqlStatementContext, characterSet));
            result.add(new MySQLEofPacket(statusFlags));
        }
        return result;
    }
    
    private Collection<Projection> getProjections(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext ? ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections() : Collections.emptyList();
    }
    
    private Collection<MySQLPacket> createParameterColumnDefinition41Packets(final SQLStatementContext sqlStatementContext, final int characterSet,
                                                                             final MySQLServerPreparedStatement serverPreparedStatement) {
        List<ShardingSphereColumn> columnsOfParameterMarkers =
                MySQLComStmtPrepareParameterMarkerExtractor.findColumnsOfParameterMarkers(sqlStatementContext.getSqlStatement(), getSchema(sqlStatementContext));
        Collection<ParameterMarkerSegment> parameterMarkerSegments = sqlStatementContext.getSqlStatement().getParameterMarkers();
        Collection<MySQLPacket> result = new ArrayList<>(parameterMarkerSegments.size());
        Collection<Integer> paramColumnDefinitionFlags = new ArrayList<>(parameterMarkerSegments.size());
        for (int index = 0; index < parameterMarkerSegments.size(); index++) {
            ShardingSphereColumn column = null;
            if (!columnsOfParameterMarkers.isEmpty() && index < columnsOfParameterMarkers.size()) {
                column = columnsOfParameterMarkers.get(index);
            }
            if (null != column) {
                int columnDefinitionFlag = calculateColumnDefinitionFlag(column);
                result.add(createMySQLColumnDefinition41Packet(characterSet, columnDefinitionFlag, MySQLBinaryColumnType.valueOfJDBCType(column.getDataType())));
                paramColumnDefinitionFlags.add(columnDefinitionFlag);
            } else {
                result.add(createMySQLColumnDefinition41Packet(characterSet, 0, MySQLBinaryColumnType.VAR_STRING));
                paramColumnDefinitionFlags.add(0);
            }
        }
        serverPreparedStatement.getParameterColumnDefinitionFlags().addAll(paramColumnDefinitionFlags);
        return result;
    }
    
    private Collection<MySQLPacket> createProjectionColumnDefinition41Packets(final SelectStatementContext selectStatementContext, final int characterSet) {
        Collection<Projection> projections = selectStatementContext.getProjectionsContext().getExpandProjections();
        ShardingSphereSchema schema = getSchema(selectStatementContext);
        Collection<MySQLPacket> result = new ArrayList<>(projections.size());
        for (Projection each : projections) {
            // TODO Calculate column definition flag for other projection types
            if (each instanceof ColumnProjection) {
                result.add(Optional.ofNullable(schema.getTable(((ColumnProjection) each).getOriginalTable().getValue()))
                        .map(table -> table.getColumn(((ColumnProjection) each).getOriginalColumn().getValue()))
                        .map(column -> createMySQLColumnDefinition41Packet(characterSet, calculateColumnDefinitionFlag(column), MySQLBinaryColumnType.valueOfJDBCType(column.getDataType())))
                        .orElseGet(() -> createMySQLColumnDefinition41Packet(characterSet, 0, MySQLBinaryColumnType.VAR_STRING)));
            } else {
                result.add(createMySQLColumnDefinition41Packet(characterSet, 0, MySQLBinaryColumnType.VAR_STRING));
            }
        }
        return result;
    }
    
    private ShardingSphereSchema getSchema(final SQLStatementContext sqlStatementContext) {
        String databaseName = sqlStatementContext.getTablesContext().getDatabaseName().orElseGet(connectionSession::getCurrentDatabaseName);
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        return sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema)
                .orElseGet(() -> database.getSchema(new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName())));
    }
    
    private int calculateColumnDefinitionFlag(final ShardingSphereColumn column) {
        int result = 0;
        result |= column.isPrimaryKey() ? MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue() : 0;
        result |= column.isUnsigned() ? MySQLColumnDefinitionFlag.UNSIGNED.getValue() : 0;
        return result;
    }
    
    private MySQLColumnDefinition41Packet createMySQLColumnDefinition41Packet(final int characterSet, final int columnDefinitionFlag, final MySQLBinaryColumnType columnType) {
        return new MySQLColumnDefinition41Packet(characterSet, columnDefinitionFlag, "", "", "", "", "", 0, columnType, 0, false);
    }
}
