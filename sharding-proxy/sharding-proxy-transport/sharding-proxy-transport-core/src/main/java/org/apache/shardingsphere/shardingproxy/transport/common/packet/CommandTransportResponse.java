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

package org.apache.shardingsphere.shardingproxy.transport.common.packet;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.spi.DatabasePacket;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Command transport response.
 *
 * @author zhangyonglun
 */
public final class CommandTransportResponse implements TransportResponse {
    
    private final boolean hasMoreData;
    
    @Getter
    private final Collection<DatabasePacket> packets = new LinkedList<>();
    
    public CommandTransportResponse() {
        this.hasMoreData = false;
    }
    
    public CommandTransportResponse(final DatabasePacket databasePacket) {
        this.hasMoreData = false;
        packets.add(databasePacket);
    }
    
    public CommandTransportResponse(final boolean hasMoreData, final Collection<DatabasePacket> databasePackets) {
        this.hasMoreData = hasMoreData;
        packets.addAll(databasePackets);
    }
    
    /**
     * Get head packet.
     *
     * @return head packet
     */
    public DatabasePacket getHeadPacket() {
        return packets.iterator().next();
    }
    
    @Override
    public boolean hasMoreData() {
        return hasMoreData;
    }
}
