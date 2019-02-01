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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;

import java.util.Collection;
import java.util.List;

/**
 * Query response packets.
 *
 * @author zhangliang
 * @author zhangyonglun
 */
@Getter
public final class QueryResponsePackets extends CommandResponsePackets {
    
    private final List<Integer> columnTypes;
    
    private final int fieldCount;
    
    private final Collection<DataHeaderPacket> dataHeaderPackets;
    
    private int sequenceId;
    
    public QueryResponsePackets(final List<Integer> columnTypes, final int fieldCount, final Collection<DataHeaderPacket> dataHeaderPackets, final int sequenceId) {
        this.columnTypes = columnTypes;
        this.fieldCount = fieldCount;
        getPackets().addAll(dataHeaderPackets);
        this.dataHeaderPackets = dataHeaderPackets;
        this.sequenceId = sequenceId;
    }
}
