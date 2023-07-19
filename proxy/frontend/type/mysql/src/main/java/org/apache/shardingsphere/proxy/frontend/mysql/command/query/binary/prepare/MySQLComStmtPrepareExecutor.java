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
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIdGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private final MySQLComStmtPreparePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        failedIfContainsMultiStatements();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL").getType()).parse(packet.getSQL(), true);
        if (!MySQLComStmtPrepareChecker.isAllowedStatement(sqlStatement)) {
            throw new UnsupportedPreparedStatementException();
        }
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(),
                sqlStatement, connectionSession.getDefaultDatabaseName());
        int statementId = MySQLStatementIdGenerator.getInstance().nextStatementId(connectionSession.getConnectionId());
        MySQLServerPreparedStatement serverPreparedStatement = new MySQLServerPreparedStatement(packet.getSQL(), sqlStatementContext, packet.getHintValueContext(), new CopyOnWriteArrayList<>());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        return createPackets(sqlStatementContext, statementId, serverPreparedStatement);
    }
    
    private void failedIfContainsMultiStatements() {
        // TODO Multi statements should be identified by SQL Parser instead of checking if sql contains ";".
        if (connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)
                && MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON == connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()
                && packet.getSQL().contains(";")) {
            throw new UnsupportedPreparedStatementException();
        }
    }
    
    private Collection<DatabasePacket> createPackets(final SQLStatementContext sqlStatementContext, final int statementId, final MySQLServerPreparedStatement serverPreparedStatement) {
        Collection<DatabasePacket> result = new LinkedList<>();
        List<Projection> projections = getProjections(sqlStatementContext);
        int parameterCount = sqlStatementContext.getSqlStatement().getParameterCount();
        result.add(new MySQLComStmtPrepareOKPacket(statementId, projections.size(), parameterCount, 0));
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        int statusFlags = ServerStatusFlagCalculator.calculateFor(connectionSession);
        if (parameterCount > 0) {
            result.addAll(createParameterColumnDefinition41Packets(sqlStatementContext, characterSet, serverPreparedStatement));
            result.add(new MySQLEofPacket(statusFlags));
        }
        if (!projections.isEmpty()) {
            result.addAll(createProjectionColumnDefinition41Packets((SelectStatementContext) sqlStatementContext, characterSet));
            result.add(new MySQLEofPacket(statusFlags));
        }
        return result;
    }
    
    private List<Projection> getProjections(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext ? ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections() : Collections.emptyList();
    }
    
    private Collection<MySQLPacket> createParameterColumnDefinition41Packets(final SQLStatementContext sqlStatementContext, final int characterSet,
                                                                             final MySQLServerPreparedStatement serverPreparedStatement) {
        Map<ParameterMarkerSegment, ShardingSphereColumn> columnsOfParameterMarkers =
                MySQLComStmtPrepareParameterMarkerExtractor.findColumnsOfParameterMarkers(sqlStatementContext.getSqlStatement(), getSchema(sqlStatementContext));
        Collection<ParameterMarkerSegment> parameterMarkerSegments = ((AbstractSQLStatement) sqlStatementContext.getSqlStatement()).getParameterMarkerSegments();
        Collection<MySQLPacket> result = new ArrayList<>(parameterMarkerSegments.size());
        for (ParameterMarkerSegment each : parameterMarkerSegments) {
            ShardingSphereColumn column = columnsOfParameterMarkers.get(each);
            if (null != column) {
                int columnDefinitionFlag = calculateColumnDefinitionFlag(column);
                result.add(createMySQLColumnDefinition41Packet(characterSet, columnDefinitionFlag, MySQLBinaryColumnType.valueOfJDBCType(column.getDataType())));
                serverPreparedStatement.getParameterColumnDefinitionFlags().add(columnDefinitionFlag);
            } else {
                result.add(createMySQLColumnDefinition41Packet(characterSet, 0, MySQLBinaryColumnType.VAR_STRING));
                serverPreparedStatement.getParameterColumnDefinitionFlags().add(0);
            }
        }
        return result;
    }
    
    private Collection<MySQLPacket> createProjectionColumnDefinition41Packets(final SelectStatementContext selectStatementContext, final int characterSet) {
        Collection<Projection> projections = selectStatementContext.getProjectionsContext().getExpandProjections();
        ShardingSphereSchema schema = getSchema(selectStatementContext);
        Map<String, String> columnToTableMap = selectStatementContext.getTablesContext()
                .findTableNamesByColumnProjection(projections.stream().filter(ColumnProjection.class::isInstance).map(ColumnProjection.class::cast).collect(Collectors.toList()), schema);
        Collection<MySQLPacket> result = new ArrayList<>(projections.size());
        for (Projection each : projections) {
            // TODO Calculate column definition flag for other projection types
            if (each instanceof ColumnProjection && null != ((ColumnProjection) each).getOriginalName()) {
                result.add(Optional.ofNullable(columnToTableMap.get(each.getColumnName())).map(schema::getTable)
                        .map(table -> table.getColumns().get(((ColumnProjection) each).getOriginalName().getValue()))
                        .map(column -> createMySQLColumnDefinition41Packet(characterSet, calculateColumnDefinitionFlag(column), MySQLBinaryColumnType.valueOfJDBCType(column.getDataType())))
                        .orElseGet(() -> createMySQLColumnDefinition41Packet(characterSet, 0, MySQLBinaryColumnType.VAR_STRING)));
            } else {
                result.add(createMySQLColumnDefinition41Packet(characterSet, 0, MySQLBinaryColumnType.VAR_STRING));
            }
        }
        return result;
    }
    
    private ShardingSphereSchema getSchema(final SQLStatementContext sqlStatementContext) {
        String databaseName = sqlStatementContext.getTablesContext().getDatabaseName().orElseGet(connectionSession::getDefaultDatabaseName);
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        return sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema)
                .orElseGet(() -> database.getSchema(DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName())));
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
