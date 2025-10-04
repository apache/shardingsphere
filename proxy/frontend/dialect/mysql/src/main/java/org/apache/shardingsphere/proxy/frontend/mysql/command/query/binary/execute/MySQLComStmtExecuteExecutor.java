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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.binary.BinaryRow;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.MySQLBinaryResultSetRowPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder.ResponsePacketBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * COM_STMT_EXECUTE command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComStmtExecuteExecutor implements QueryCommandExecutor {
    
    private final MySQLComStmtExecutePacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ProxyBackendHandler proxyBackendHandler;
    
    @Getter
    private ResponseType responseType;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        MySQLServerPreparedStatement preparedStatement = updateAndGetPreparedStatement();
        List<Object> params = packet.readParameters(preparedStatement.getParameterTypes(), preparedStatement.getLongData().keySet(), preparedStatement.getParameterColumnDefinitionFlags());
        preparedStatement.getLongData().forEach(params::set);
        SQLStatementContext sqlStatementContext = preparedStatement.getSqlStatementContext();
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).bindParameters(params);
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, preparedStatement.getSql(), params, preparedStatement.getHintValueContext(), connectionSession.getConnectionContext(),
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(), true);
        proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(TypedSPILoader.getService(DatabaseType.class, "MySQL"), queryContext, connectionSession, true);
        ResponseHeader responseHeader = proxyBackendHandler.execute();
        return responseHeader instanceof QueryResponseHeader ? processQuery((QueryResponseHeader) responseHeader) : processUpdate((UpdateResponseHeader) responseHeader);
    }
    
    private MySQLServerPreparedStatement updateAndGetPreparedStatement() {
        MySQLServerPreparedStatement result = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(packet.getStatementId());
        if (MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST == packet.getNewParametersBoundFlag()) {
            result.getParameterTypes().clear();
            result.getParameterTypes().addAll(packet.getNewParameterTypes());
        }
        return result;
    }
    
    private Collection<DatabasePacket> processQuery(final QueryResponseHeader queryResponseHeader) {
        responseType = ResponseType.QUERY;
        int characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
        return ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, characterSet, ServerStatusFlagCalculator.calculateFor(connectionSession, true));
    }
    
    private Collection<DatabasePacket> processUpdate(final UpdateResponseHeader updateResponseHeader) {
        responseType = ResponseType.UPDATE;
        return ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader, ServerStatusFlagCalculator.calculateFor(connectionSession, true));
    }
    
    @Override
    public boolean next() throws SQLException {
        return proxyBackendHandler.next();
    }
    
    @Override
    public MySQLPacket getQueryRowPacket() throws SQLException {
        QueryResponseRow queryResponseRow = proxyBackendHandler.getRowData();
        return new MySQLBinaryResultSetRowPacket(createBinaryRow(queryResponseRow));
    }
    
    private BinaryRow createBinaryRow(final QueryResponseRow queryResponseRow) {
        List<BinaryCell> result = new ArrayList<>(queryResponseRow.getCells().size());
        for (QueryResponseCell each : queryResponseRow.getCells()) {
            result.add(new BinaryCell(MySQLBinaryColumnType.valueOfJDBCType(each.getJdbcType()), each.getData()));
        }
        return new BinaryRow(result);
    }
    
    @Override
    public void close() throws SQLException {
        if (null != proxyBackendHandler) {
            proxyBackendHandler.close();
        }
    }
}
