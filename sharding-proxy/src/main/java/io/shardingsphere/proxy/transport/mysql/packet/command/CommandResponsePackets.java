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

package io.shardingsphere.proxy.transport.mysql.packet.command;

import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Command response packet.
 *
 * @author zhangyonglun
 */
@NoArgsConstructor
@Getter
public final class CommandResponsePackets {
    
    private final Collection<DatabaseProtocolPacket> databaseProtocolPackets = new LinkedList<>();
    
    public CommandResponsePackets(final DatabaseProtocolPacket databaseProtocolPacket) {
        databaseProtocolPackets.add(databaseProtocolPacket);
    }
    
    /**
     * Add packet.
     *
     * @param databaseProtocolPacket database protocol packet
     */
    public void addPacket(final DatabaseProtocolPacket databaseProtocolPacket) {
        databaseProtocolPackets.add(databaseProtocolPacket);
    }
    
    /**
     * Add packets.
     *
     * @param databaseProtocolPackets database protocol packets
     */
    public void addPackets(final Collection<DatabaseProtocolPacket> databaseProtocolPackets) {
        databaseProtocolPackets.addAll(databaseProtocolPackets);
    }
    
    /**
     * Get head packet.
     *
     * @return head database protocol packet
     */
    public DatabaseProtocolPacket getHeadPacket() {
        return databaseProtocolPackets.iterator().next();
    }
    
    /**
     * Size of databaseProtocolPackets.
     *
     * @return size
     */
    public int size() {
        return databaseProtocolPackets.size();
    }
}
