/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.packet.command;

import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Generated;
import lombok.Getter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Command packet.
 *
 * @author zhangliang
 */
public class CommandResponsePackets {
    
    @Getter
    private final List<DatabaseProtocolPacket> databaseProtocolPackets;
    
//    private final Iterator<DatabaseProtocolPacket> packetIterator;
    
    public CommandResponsePackets() {
        databaseProtocolPackets = new LinkedList<>();
//        packetIterator = databaseProtocolPackets.iterator();
    }
    
    public CommandResponsePackets(DatabaseProtocolPacket databaseProtocolPacket) {
        databaseProtocolPackets = new LinkedList<>();
        databaseProtocolPackets.add(databaseProtocolPacket);
    }
    
    public boolean addPacket(final DatabaseProtocolPacket databaseProtocolPacket) {
        return databaseProtocolPackets.add(databaseProtocolPacket);
    }
    
    public DatabaseProtocolPacket getPacket(final int index) {
        return databaseProtocolPackets.get(index);
    }
    
//    public DatabaseProtocolPacket nextPacket() {
//        return packetIterator.next();
//    }
    
    public int size() {
        return databaseProtocolPackets.size();
    }
}
