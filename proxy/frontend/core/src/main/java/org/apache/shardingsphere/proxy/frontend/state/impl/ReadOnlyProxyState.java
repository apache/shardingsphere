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

package org.apache.shardingsphere.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.exception.ReadOnlyException;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyState;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Optional;

/**
 * ReadOnly proxy state.
 */
public final class ReadOnlyProxyState implements ProxyState {
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        if (isPrimaryRoute(connectionSession.getQueryContext().getSqlStatementContext())) {
            context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(new ReadOnlyException()));
            Optional<DatabasePacket<?>> databasePacket = databaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket(connectionSession);
            databasePacket.ifPresent(context::writeAndFlush);
        }
    }
    
    private boolean isPrimaryRoute(final SQLStatementContext<?> sqlStatementContext) {
        return isWriteRouteStatement(sqlStatementContext) || isHintWriteRouteOnly(sqlStatementContext);
    }
    
    private boolean isWriteRouteStatement(final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        return containsLockSegment(sqlStatement) || containsLastInsertIdProjection(sqlStatementContext) || !(sqlStatement instanceof SelectStatement);
    }
    
    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && SelectStatementHandler.getLockSegment((SelectStatement) sqlStatement).isPresent();
    }
    
    private boolean containsLastInsertIdProjection(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getProjectionsContext().isContainsLastInsertIdProjection();
    }
    
    private boolean isHintWriteRouteOnly(final SQLStatementContext<?> sqlStatementContext) {
        return HintManager.isWriteRouteOnly() || (sqlStatementContext instanceof CommonSQLStatementContext && ((CommonSQLStatementContext<?>) sqlStatementContext).isHintWriteRouteOnly());
    }
}
