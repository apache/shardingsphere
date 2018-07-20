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

package io.shardingsphere.proxy.backend.common.jdbc.execute;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL execute responses.
 * 
 * @author zhangliang
 */
@Getter
public final class SQLExecuteResponses {
    
    private final List<CommandResponsePackets> commandResponsePacketsList;
    
    private final List<QueryResult> queryResults;
    
    private final CommandResponsePackets firstCommandResponsePackets;
    
    public SQLExecuteResponses(final List<CommandResponsePackets> commandResponsePacketsList, final List<QueryResult> queryResults) {
        this.commandResponsePacketsList = commandResponsePacketsList;
        this.queryResults = queryResults;
        firstCommandResponsePackets = commandResponsePacketsList.iterator().next();
    }
    
    /**
     * Judge SQL executed is query or not.
     * 
     * @return is query or not
     */
    public boolean isQuery() {
        for (DatabasePacket each : firstCommandResponsePackets.getPackets()) {
            if (each instanceof ColumnDefinition41Packet) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get column types.
     * 
     * @return column types
     */
    public List<ColumnType> getColumnTypes() {
        List<ColumnType> result = new ArrayList<>(firstCommandResponsePackets.getPackets().size());
        for (DatabasePacket each : firstCommandResponsePackets.getPackets()) {
            if (each instanceof ColumnDefinition41Packet) {
                result.add(((ColumnDefinition41Packet) each).getColumnType());
            }
        }
        return result;
    }
    
    /**
     * Get column count.
     * 
     * @return column count
     */
    public int getColumnCount() {
        return 1 == firstCommandResponsePackets.getPackets().size() ? 0 : firstCommandResponsePackets.getPackets().size() - 2;
    }
}
