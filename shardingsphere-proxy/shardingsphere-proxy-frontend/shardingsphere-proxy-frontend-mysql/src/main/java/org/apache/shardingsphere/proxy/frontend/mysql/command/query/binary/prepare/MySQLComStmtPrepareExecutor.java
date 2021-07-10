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

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private static final MySQLBinaryStatementRegistry PREPARED_STATEMENT_REGISTRY = MySQLBinaryStatementRegistry.getInstance();
    
    private final MySQLComStmtPreparePacket packet;
    
    private final BackendConnection backendConnection;
    
    private int currentSequenceId;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                ProxyContext.getInstance().getMetaDataContexts().getMetaData(backendConnection.getSchemaName()).getResource().getDatabaseType()));
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(packet.getSql(), true);
        if (!MySQLComStmtPrepareChecker.isStatementAllowed(sqlStatement)) {
            throw new UnsupportedPreparedStatementException();
        }
        int parameterCount = sqlStatement.getParameterCount();
        int projectionCount = getProjectionCount(sqlStatement);
        int statementId = PREPARED_STATEMENT_REGISTRY.register(packet.getSql(), parameterCount);
        return createPackets(statementId, projectionCount, parameterCount);
    }
    
    private int getProjectionCount(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement
                ? getProjectionCount((SelectStatement) sqlStatement) : 0;
    }

    private int getProjectionCount(SelectStatement selectStatement) {
        ProjectionsSegment projections = selectStatement.getProjections();
        Collection<ProjectionSegment> projectionSegments = projections.getProjections();
        return isShorthandProjectionAndSimpleTable(projectionSegments, selectStatement)
            ? getTableColumnCount((SimpleTableSegment) selectStatement.getFrom())
            : projectionSegments.size();
    }

    private boolean isShorthandProjectionAndSimpleTable(Collection<ProjectionSegment> c, SelectStatement statement) {
        return isShorthandProjection(c) && isSimpleTableStatement(statement);
    }

    private boolean isShorthandProjection(Collection<ProjectionSegment> c) {
        return c.size() == 1 && c.iterator().next() instanceof ShorthandProjectionSegment;
    }

    private boolean isSimpleTableStatement(SelectStatement statement) {
        return statement.getFrom() instanceof SimpleTableSegment;
    }

    private int getTableColumnCount(SimpleTableSegment table) {
        String schemaName = backendConnection.getSchemaName();
        ShardingSphereMetaData shardingSphereMetaData = ProxyContext.getInstance().getMetaData(schemaName);

        String tableName = table.getTableName().getIdentifier().getValue();
        TableMetaData tableMetaData = shardingSphereMetaData.getSchema().get(tableName);
        Preconditions.checkNotNull(tableMetaData, "Table '%s' not found", tableName);

        return tableMetaData.getColumns().size();
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
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, "", "", "", "?", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
    
    private Collection<DatabasePacket<?>> createProjectionColumnDefinition41Packets(final int projectionCount) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (int i = 0; i < projectionCount; i++) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, "", "", "", "", "", 0, MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING, 0, false));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
}
