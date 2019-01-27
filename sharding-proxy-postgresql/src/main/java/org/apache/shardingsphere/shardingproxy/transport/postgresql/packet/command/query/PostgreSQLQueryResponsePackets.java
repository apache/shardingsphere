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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandResponsePackets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL query response packets.
 *
 * @author zhangyonglun
 */
public final class PostgreSQLQueryResponsePackets extends PostgreSQLCommandResponsePackets {
    
    private final FieldCountPacket fieldCountPacket;
    
    @Getter
    private final Collection<ColumnDefinition41Packet> columnDefinition41Packets;
    
    public PostgreSQLQueryResponsePackets(final FieldCountPacket fieldCountPacket, final Collection<ColumnDefinition41Packet> columnDefinition41Packets, final EofPacket eofPacket) {
        getPackets().add(fieldCountPacket);
        getPackets().addAll(columnDefinition41Packets);
        getPackets().add(eofPacket);
        this.fieldCountPacket = fieldCountPacket;
        this.columnDefinition41Packets = columnDefinition41Packets;
    }
    
    /**
     * Get column count.
     *
     * @return column count
     */
    public int getColumnCount() {
        return fieldCountPacket.getColumnCount();
    }
    
    /**
     * Get column types.
     *
     * @return column types
     */
    public List<ColumnType> getColumnTypes() {
        List<ColumnType> result = new LinkedList<>();
        for (ColumnDefinition41Packet each : columnDefinition41Packets) {
            result.add(each.getColumnType());
        }
        return result;
    }
}
