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

package org.apache.shardingsphere.proxy.frontend.opengauss.command.query.simple;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Command query executor for openGauss.
 */
public final class OpenGaussComQueryExecutor implements QueryCommandExecutor {
    
    private final PortalContext portalContext;
    
    private final ProxyBackendHandler proxyBackendHandler;
    
    @Getter
    private volatile ResponseType responseType;
    
    public OpenGaussComQueryExecutor(final PortalContext portalContext, final PostgreSQLComQueryPacket comQueryPacket, final ConnectionSession connectionSession) throws SQLException {
        this.portalContext = portalContext;
        proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(TypedSPILoader.getService(DatabaseType.class, "openGauss"), comQueryPacket.getSQL(), connectionSession,
                comQueryPacket.getHintValueContext());
    }
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        ResponseHeader responseHeader = proxyBackendHandler.execute();
        if (responseHeader instanceof QueryResponseHeader) {
            return Collections.singleton(createRowDescriptionPacket((QueryResponseHeader) responseHeader));
        }
        responseType = ResponseType.UPDATE;
        return createUpdatePacket((UpdateResponseHeader) responseHeader);
    }
    
    private PostgreSQLRowDescriptionPacket createRowDescriptionPacket(final QueryResponseHeader queryResponseHeader) {
        responseType = ResponseType.QUERY;
        return new PostgreSQLRowDescriptionPacket(createColumnDescriptions(queryResponseHeader));
    }
    
    private Collection<PostgreSQLColumnDescription> createColumnDescriptions(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponseHeader.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each.getColumnLabel(), ++columnIndex, each.getColumnType(), each.getColumnLength(), each.getColumnTypeName()));
        }
        return result;
    }
    
    private Collection<DatabasePacket> createUpdatePacket(final UpdateResponseHeader updateResponseHeader) throws SQLException {
        SQLStatement sqlStatement = updateResponseHeader.getSqlStatement();
        if (sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement) {
            portalContext.closeAll();
        }
        if (sqlStatement instanceof SetStatement) {
            return createParameterStatusResponse((SetStatement) sqlStatement);
        }
        return Collections.singletonList(sqlStatement instanceof EmptyStatement ? new PostgreSQLEmptyQueryResponsePacket()
                : new PostgreSQLCommandCompletePacket(PostgreSQLCommand.valueOf(sqlStatement.getClass()).map(PostgreSQLCommand::getTag).orElse(""), updateResponseHeader.getUpdateCount()));
    }
    
    private Collection<DatabasePacket> createParameterStatusResponse(final SetStatement sqlStatement) {
        Collection<DatabasePacket> result = new ArrayList<>(2);
        result.add(new PostgreSQLCommandCompletePacket("SET", 0));
        for (VariableAssignSegment each : sqlStatement.getVariableAssigns()) {
            result.add(new PostgreSQLParameterStatusPacket(each.getVariable().getVariable(), IdentifierValue.getQuotedContent(each.getAssignValue())));
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return proxyBackendHandler.next();
    }
    
    @Override
    public PostgreSQLPacket getQueryRowPacket() throws SQLException {
        return new PostgreSQLDataRowPacket(proxyBackendHandler.getRowData().getData());
    }
    
    @Override
    public void close() throws SQLException {
        proxyBackendHandler.close();
    }
}
