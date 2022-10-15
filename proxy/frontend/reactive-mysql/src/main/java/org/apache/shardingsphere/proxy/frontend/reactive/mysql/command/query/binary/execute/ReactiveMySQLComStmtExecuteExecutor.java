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

import io.vertx.core.Future;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.binary.BinaryRow;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLBinaryResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
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

/**
 * Reactive COM_STMT_EXECUTE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class ReactiveMySQLComStmtExecuteExecutor implements ReactiveCommandExecutor {
    
    private final MySQLComStmtExecutePacket packet;
    
    private final ConnectionSession connectionSession;
    
    private VertxDatabaseCommunicationEngine databaseCommunicationEngine;
    
    private ProxyBackendHandler proxyBackendHandler;
    
    @Getter
    private ResponseType responseType;
    
    private int currentSequenceId;
    
    @SneakyThrows(SQLException.class)
    @Override
    public Future<Collection<DatabasePacket<?>>> executeFuture() {
        MySQLServerPreparedStatement preparedStatement = updateAndGetPreparedStatement();
        List<Object> parameters = packet.readParameters(preparedStatement.getParameterTypes(), preparedStatement.getLongData().keySet());
        preparedStatement.getLongData().forEach(parameters::set);
        SQLStatementContext<?> sqlStatementContext = preparedStatement.getSqlStatementContext().get();
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(parameters);
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, preparedStatement.getSql(), parameters);
        connectionSession.setQueryContext(queryContext);
        SQLStatement sqlStatement = preparedStatement.getSqlStatement();
        String databaseName = connectionSession.getDatabaseName();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLCheckEngine.check(sqlStatementContext, Collections.emptyList(), getRules(databaseName), databaseName, metaDataContexts.getMetaData().getDatabases(), connectionSession.getGrantee());
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        // TODO Refactor the following branch
        if (sqlStatement instanceof TCLStatement) {
            proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(DatabaseTypeFactory.getInstance("MySQL"), preparedStatement.getSql(), sqlStatement, connectionSession);
        } else {
            databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(queryContext, connectionSession.getBackendConnection(), true);
        }
        return (null != databaseCommunicationEngine ? databaseCommunicationEngine.executeFuture() : proxyBackendHandler.executeFuture()).compose(responseHeader -> {
            Collection<DatabasePacket<?>> headerPackets = responseHeader instanceof QueryResponseHeader ? processQuery((QueryResponseHeader) responseHeader, characterSet)
                    : processUpdate((UpdateResponseHeader) responseHeader);
            List<DatabasePacket<?>> result = new LinkedList<>(headerPackets);
            if (ResponseType.UPDATE == responseType) {
                return Future.succeededFuture(result);
            }
            try {
                while (next()) {
                    result.add(getQueryRowPacket());
                }
                result.add(new MySQLEofPacket(++currentSequenceId, ServerStatusFlagCalculator.calculateFor(connectionSession)));
                return Future.succeededFuture(result);
            } catch (SQLException ex) {
                return Future.failedFuture(ex);
            }
        });
    }
    
    private MySQLServerPreparedStatement updateAndGetPreparedStatement() {
        MySQLServerPreparedStatement result = connectionSession.getPreparedStatementRegistry().getPreparedStatement(packet.getStatementId());
        if (MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST == packet.getNewParametersBoundFlag()) {
            result.setParameterTypes(packet.getNewParameterTypes());
        }
        return result;
    }
    
    private static Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    private Collection<DatabasePacket<?>> processQuery(final QueryResponseHeader queryResponseHeader, final int characterSet) {
        responseType = ResponseType.QUERY;
        Collection<DatabasePacket<?>> result = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, characterSet, ServerStatusFlagCalculator.calculateFor(connectionSession));
        currentSequenceId = result.size();
        return result;
    }
    
    private Collection<DatabasePacket<?>> processUpdate(final UpdateResponseHeader updateResponseHeader) {
        responseType = ResponseType.UPDATE;
        return ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader, ServerStatusFlagCalculator.calculateFor(connectionSession));
    }
    
    private boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    private MySQLPacket getQueryRowPacket() throws SQLException {
        QueryResponseRow queryResponseRow = databaseCommunicationEngine.getRowData();
        return new MySQLBinaryResultSetRowPacket(++currentSequenceId, createBinaryRow(queryResponseRow));
    }
    
    private BinaryRow createBinaryRow(final QueryResponseRow queryResponseRow) {
        List<BinaryCell> result = new ArrayList<>(queryResponseRow.getCells().size());
        for (QueryResponseCell each : queryResponseRow.getCells()) {
            result.add(new BinaryCell(MySQLBinaryColumnType.valueOfJDBCType(each.getJdbcType()), each.getData()));
        }
        return new BinaryRow(result);
    }
    
    @Override
    public Future<Void> closeFuture() {
        try {
            if (null != proxyBackendHandler) {
                proxyBackendHandler.close();
            }
            return Future.succeededFuture();
        } catch (final SQLException ex) {
            return Future.failedFuture(ex);
        }
    }
}
