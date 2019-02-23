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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Execute query response unit.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class ExecuteQueryResponseUnit implements ExecuteResponseUnit {
    
    private final Collection<QueryHeader> queryHeaders;
    
    private final QueryResult queryResult;
    
    /**
     * Get query response packets.
     *
     * @return query response packets
     */
    public QueryResponsePackets getQueryResponsePackets() {
        List<Integer> columnTypes = new LinkedList<>();
        Collection<DataHeaderPacket> dataHeaderPackets = new LinkedList<>();
        int sequenceId = 1;
        for (QueryHeader each : queryHeaders) {
            columnTypes.add(each.getColumnType());
            dataHeaderPackets.add(new DataHeaderPacket(++sequenceId, each.getSchema(), each.getTable(), each.getOrgTable(), each.getName(), each.getOrgName(),
                    each.getColumnLength(), each.getColumnType(), each.getDecimals()));
        }
        return new QueryResponsePackets(columnTypes, queryHeaders.size(), dataHeaderPackets, ++sequenceId);
    }
}
