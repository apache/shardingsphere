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
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.constant.PostgreSQLColumnType;

import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL query response packets.
 *
 * @author zhangyonglun
 */
@Getter
public final class PostgreSQLQueryResponsePackets extends CommandResponsePackets {
    
    private final PostgreSQLRowDescriptionPacket postgreSQLRowDescriptionPacket;
    
    public PostgreSQLQueryResponsePackets(final PostgreSQLRowDescriptionPacket postgreSQLRowDescriptionPacket) {
        getPackets().add(postgreSQLRowDescriptionPacket);
        this.postgreSQLRowDescriptionPacket = postgreSQLRowDescriptionPacket;
    }
    
    /**
     * Get column count.
     *
     * @return column count
     */
    public int getColumnCount() {
        return postgreSQLRowDescriptionPacket.getFieldCount();
    }
    
    /**
     * Get column types.
     *
     * @return column types
     */
    public List<PostgreSQLColumnType> getColumnTypes() {
        List<PostgreSQLColumnType> result = new LinkedList<>();
        for (PostgreSQLColumnDescription each : postgreSQLRowDescriptionPacket.getPostgreSQLColumnDescriptions()) {
            result.add(PostgreSQLColumnType.valueOf(each.getTypeOID()));
        }
        return result;
    }
}
