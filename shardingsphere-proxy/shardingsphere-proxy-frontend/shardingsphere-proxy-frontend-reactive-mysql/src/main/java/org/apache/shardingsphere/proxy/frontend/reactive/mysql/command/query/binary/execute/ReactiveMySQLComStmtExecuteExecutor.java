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

package org.apache.shardingsphere.proxy.frontend.reactive.mysql.command.query.binary.execute;

import com.google.common.base.Preconditions;
import io.vertx.core.Future;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.binary.BinaryRow;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLBinaryResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementSchemaHolder;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder.ResponsePacketBuilder;
import org.apache.shardingsphere.proxy.frontend.reactive.command.executor.ReactiveCommandExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Reactive COM_STMT_EXECUTE command executor for MySQL.
 */
public final class ReactiveMySQLComStmtExecuteExecutor implements ReactiveCommandExecutor {
    
    private final VertxDatabaseCommunicationEngine databaseCommunicationEngine;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    private final int characterSet;
    
    @Getter
    private volatile ResponseType responseType;
    
    private int currentSequenceId;
    
    public ReactiveMySQLComStmtExecuteExecutor(final MySQLComStmtExecutePacket packet, final ConnectionSession connectionSession) throws SQLException {
        String schemaName = connectionSession.getSchemaName();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<SQLParserRule> sqlParserRule = metaDataContexts.getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(sqlParserRule.isPresent());
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType()), sqlParserRule.get().toParserConfiguration());
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(packet.getSql(), true);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaDataMap(), packet.getParameters(),
                sqlStatement, connectionSession.getDefaultSchemaName());
        // TODO optimize SQLStatementSchemaHolder
        if (sqlStatementContext instanceof TableAvailable) {
            ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().ifPresent(SQLStatementSchemaHolder::set);
        }
        SQLCheckEngine.check(sqlStatement, Collections.emptyList(), getRules(schemaName), schemaName, metaDataContexts.getMetaDataMap(), connectionSession.getGrantee());
        characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        // TODO Refactor the following branch
        if (sqlStatement instanceof TCLStatement) {
            databaseCommunicationEngine = null;
            textProtocolBackendHandler =
                    TextProtocolBackendHandlerFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), packet.getSql(), () -> Optional.of(sqlStatement), connectionSession);
            return;
        }
        textProtocolBackendHandler = null;
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance()
                .newBinaryProtocolInstance(sqlStatementContext, packet.getSql(), packet.getParameters(), connectionSession.getBackendConnection());
    }
    
    private static Collection<ShardingSphereRule> getRules(final String schemaName) {
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules());
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    @Override
    public Future<Collection<DatabasePacket<?>>> executeFuture() {
        return (null != databaseCommunicationEngine ? databaseCommunicationEngine.execute() : textProtocolBackendHandler.executeFuture()).compose(responseHeader -> {
            Collection<DatabasePacket<?>> headerPackets = responseHeader instanceof QueryResponseHeader ? processQuery((QueryResponseHeader) responseHeader)
                    : processUpdate((UpdateResponseHeader) responseHeader);
            List<DatabasePacket<?>> result = new LinkedList<>(headerPackets);
            if (ResponseType.UPDATE == responseType) {
                return Future.succeededFuture(result);
            }
            try {
                while (next()) {
                    result.add(getQueryRowPacket());
                }
                result.add(new MySQLEofPacket(++currentSequenceId));
                return Future.succeededFuture(result);
            } catch (SQLException ex) {
                return Future.failedFuture(ex);
            }
        });
    }
    
    private Collection<DatabasePacket<?>> processQuery(final QueryResponseHeader queryResponseHeader) {
        responseType = ResponseType.QUERY;
        Collection<DatabasePacket<?>> result = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, characterSet);
        currentSequenceId = result.size();
        return result;
    }
    
    private Collection<DatabasePacket<?>> processUpdate(final UpdateResponseHeader updateResponseHeader) {
        responseType = ResponseType.UPDATE;
        return ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader);
    }
    
    private boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    private MySQLPacket getQueryRowPacket() throws SQLException {
        QueryResponseRow queryResponseRow = databaseCommunicationEngine.getQueryResponseRow();
        return new MySQLBinaryResultSetRowPacket(++currentSequenceId, createBinaryRow(queryResponseRow));
    }
    
    private BinaryRow createBinaryRow(final QueryResponseRow queryResponseRow) {
        List<BinaryCell> result = new ArrayList<>(queryResponseRow.getCells().size());
        for (QueryResponseCell each : queryResponseRow.getCells()) {
            result.add(new BinaryCell(MySQLBinaryColumnType.valueOfJDBCType(((BinaryQueryResponseCell) each).getJdbcType()), each.getData()));
        }
        return new BinaryRow(result);
    }
    
    @Override
    public Future<Void> closeFuture() {
        try {
            if (null != textProtocolBackendHandler) {
                textProtocolBackendHandler.close();
            }
            return Future.succeededFuture();
        } catch (final SQLException ex) {
            return Future.failedFuture(ex);
        }
    }
}
