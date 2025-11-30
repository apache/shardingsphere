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

import lombok.RequiredArgsConstructor;
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
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Firebird fetch statement command executor.
 */
@RequiredArgsConstructor
public final class FirebirdFetchStatementCommandExecutor implements CommandExecutor {
    
    private final FirebirdFetchStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        Collection<DatabasePacket> result = new LinkedList<>();
        ProxyBackendHandler proxyBackendHandler = FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(connectionSession.getConnectionId(), packet.getStatementId());
        if (null == proxyBackendHandler) {
            result.add(FirebirdFetchResponsePacket.getFetchNoMoreRowsPacket());
            return result;
        }
        for (int i = 0; i < packet.getFetchSize(); i++) {
            if (proxyBackendHandler.next()) {
                QueryResponseRow queryResponseRow = proxyBackendHandler.getRowData();
                BinaryRow row = createBinaryRow(queryResponseRow);
                result.add(FirebirdFetchResponsePacket.getFetchRowPacket(row));
            } else {
                connectionSession.getDatabaseConnectionManager().unmarkResourceInUse(proxyBackendHandler);
                result.add(FirebirdFetchResponsePacket.getFetchNoMoreRowsPacket());
                return result;
            }
        }
        result.add(FirebirdFetchResponsePacket.getFetchEndPacket());
        return result;
    }
    
    private BinaryRow createBinaryRow(final QueryResponseRow queryResponseRow) {
        List<BinaryCell> result = new ArrayList<>(queryResponseRow.getCells().size());
        for (QueryResponseCell each : queryResponseRow.getCells()) {
            result.add(new BinaryCell(FirebirdBinaryColumnType.valueOfJDBCType(each.getJdbcType()), each.getData()));
        }
        return new BinaryRow(result);
    }
}
