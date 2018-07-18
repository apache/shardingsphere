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

package io.shardingsphere.proxy.backend.common.jdbc;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * JDBC execute response.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class JDBCExecuteResponse {
    
    private final CommandResponsePackets commandResponsePackets;
    
    private final QueryResult queryResult;
    
    JDBCExecuteResponse(final CommandResponsePackets commandResponsePackets) {
        this(commandResponsePackets, null);
    }
    
    /**
     * Get column types.
     * 
     * @return column types
     */
    public List<ColumnType> getColumnTypes() {
        List<ColumnType> result = new ArrayList<>(commandResponsePackets.getDatabaseProtocolPackets().size());
        for (DatabaseProtocolPacket each : commandResponsePackets.getDatabaseProtocolPackets()) {
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
        return 1 == commandResponsePackets.getDatabaseProtocolPackets().size() ? 0 : commandResponsePackets.getDatabaseProtocolPackets().size() - 2;
    }
}
