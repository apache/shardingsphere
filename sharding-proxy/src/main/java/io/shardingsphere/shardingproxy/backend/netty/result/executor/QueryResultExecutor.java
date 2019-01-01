/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.netty.result.executor;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.shardingproxy.backend.netty.NettyBackendHandler;
import io.shardingsphere.shardingproxy.backend.netty.client.response.mysql.MySQLQueryResult;
import io.shardingsphere.shardingproxy.backend.netty.result.collector.QueryResultCollector;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutor;
import io.shardingsphere.shardingproxy.runtime.RuntimeContext;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Query result executor for processing query results.
 *
 * @author wuxu
 */
@RequiredArgsConstructor
public class QueryResultExecutor implements Runnable {
    
    private final QueryResultCollector queryResultCollector;
    
    @Override
    public final void run() {
        processResults();
    }
    
    private void processResults() {
        CommandExecutor commandExecutor = RuntimeContext.getInstance().getUniqueCommandExecutor().get(queryResultCollector.getCommandPacketId());
        //todo: it works well of commandExecutor writing result, but we should optimize to remove null condition
        if (commandExecutor == null) {
            return;
        }
        List<QueryResult> queryResults = queryResultCollector.getResponses();
        NettyBackendHandler nettyBackendHandler = queryResultCollector.getNettyBackendHandler();
        SQLStatement sqlStatement = queryResultCollector.getSqlStatement();
        List<CommandResponsePackets> packets = new LinkedList<>();
        for (QueryResult each : queryResults) {
            MySQLQueryResult queryResult0 = (MySQLQueryResult) each;
            if (0 == nettyBackendHandler.getCurrentSequenceId()) {
                nettyBackendHandler.setCurrentSequenceId(queryResult0.getCurrentSequenceId());
            }
            if (0 == nettyBackendHandler.getColumnCount()) {
                nettyBackendHandler.setColumnCount(queryResult0.getColumnCount());
            }
            packets.add(queryResult0.getCommandResponsePackets());
        }
        CommandResponsePackets responsePackets = nettyBackendHandler.merge(sqlStatement, packets, queryResults);
        if (!queryResultCollector.isMasterSlaveSchema()) {
            nettyBackendHandler.refreshTableMetaData(queryResultCollector.getLogicSchema(), sqlStatement);
        }
        commandExecutor.writeResult(responsePackets);
    }
}
