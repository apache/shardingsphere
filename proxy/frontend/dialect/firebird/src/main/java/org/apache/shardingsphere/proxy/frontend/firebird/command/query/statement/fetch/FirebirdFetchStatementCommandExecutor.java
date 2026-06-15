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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch;

import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.binary.BinaryRow;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdFetchResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Firebird fetch statement command executor.
 */
public final class FirebirdFetchStatementCommandExecutor implements QueryCommandExecutor {
    
    private final FirebirdFetchStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private final ProxyBackendHandler proxyBackendHandler;
    
    private int fetchCount;
    
    public FirebirdFetchStatementCommandExecutor(final FirebirdFetchStatementPacket packet, final ConnectionSession connectionSession) {
        this.packet = packet;
        this.connectionSession = connectionSession;
        proxyBackendHandler = FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(connectionSession.getConnectionId(), packet.getStatementId());
    }
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        if (proxyBackendHandler == null) {
            fetchCount = packet.getFetchSize() + 1;
            return Collections.singletonList(FirebirdFetchResponsePacket.getFetchNoMoreRowsPacket());
        }
        return Collections.singletonList(getQueryRowPacket());
    }
    
    @Override
    public ResponseType getResponseType() {
        return ResponseType.QUERY;
    }
    
    @Override
    public boolean next() throws SQLException {
        return fetchCount <= packet.getFetchSize();
    }
    
    @Override
    public DatabasePacket getQueryRowPacket() throws SQLException {
        fetchCount++;
        if (fetchCount <= packet.getFetchSize()) {
            if (proxyBackendHandler.next()) {
                BinaryRow row = createBinaryRow(proxyBackendHandler.getRowData());
                return FirebirdFetchResponsePacket.getFetchRowPacket(row);
            } else {
                connectionSession.getDatabaseConnectionManager().unmarkResourceInUse(proxyBackendHandler);
                fetchCount = packet.getFetchSize() + 1;
                return FirebirdFetchResponsePacket.getFetchNoMoreRowsPacket();
            }
        }
        return FirebirdFetchResponsePacket.getFetchEndPacket();
    }
    
    private BinaryRow createBinaryRow(final QueryResponseRow queryResponseRow) {
        List<BinaryCell> result = new ArrayList<>(queryResponseRow.getCells().size());
        for (QueryResponseCell each : queryResponseRow.getCells()) {
            result.add(new BinaryCell(FirebirdBinaryColumnType.valueOfJDBCType(each.getJdbcType()), each.getData()));
        }
        return new BinaryRow(result);
    }
}
