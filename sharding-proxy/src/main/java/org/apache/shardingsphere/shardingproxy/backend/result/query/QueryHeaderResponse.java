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

package org.apache.shardingsphere.shardingproxy.backend.result.query;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;

import java.util.Collection;
import java.util.List;

/**
 * Query header response.
 *
 * @author zhangliang
 */
@Getter
public final class QueryHeaderResponse implements BackendResponse {
    
    private final List<Integer> columnTypes;
    
    private final int fieldCount;
    
    private final List<QueryHeader> queryHeaders;
    
    private int sequenceId;
    
    public QueryHeaderResponse(final List<Integer> columnTypes, final int fieldCount, final List<QueryHeader> queryHeaders, final int sequenceId) {
        this.columnTypes = columnTypes;
        this.fieldCount = fieldCount;
        this.queryHeaders = queryHeaders;
        this.sequenceId = sequenceId;
    }
    
    @Override
    public DatabasePacket getHeadPacket() {
        return getPacket(queryHeaders.iterator().next());
    }
    
    @Override
    public Collection<DatabasePacket> getPackets() {
        return Lists.transform(queryHeaders, new Function<QueryHeader, DatabasePacket>() {
    
            @Override
            public DatabasePacket apply(final QueryHeader input) {
                return getPacket(input);
            }
        });
    }
    
    private DatabasePacket getPacket(final QueryHeader queryHeader) {
        return new DataHeaderPacket(queryHeader.getSequenceId(), queryHeader.getSchema(), queryHeader.getTable(), queryHeader.getOrgTable(),
                queryHeader.getName(), queryHeader.getOrgName(), queryHeader.getColumnLength(), queryHeader.getColumnType(), queryHeader.getDecimals());
    }
}
