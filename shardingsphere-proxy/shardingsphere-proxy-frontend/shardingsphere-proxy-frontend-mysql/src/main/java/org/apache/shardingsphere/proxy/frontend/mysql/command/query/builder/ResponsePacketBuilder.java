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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnFieldDetailFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.result.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Response packet builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponsePacketBuilder {
    
    /**
     * Build query response packets.
     * 
     * @param queryResponse query response
     * @return query response packets
     */
    public static Collection<DatabasePacket<?>> buildQueryResponsePackets(final QueryResponse queryResponse) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        int sequenceId = 0;
        List<QueryHeader> queryHeader = queryResponse.getQueryHeaders();
        result.add(new MySQLFieldCountPacket(++sequenceId, queryHeader.size()));
        for (QueryHeader each : queryHeader) {
            result.add(new MySQLColumnDefinition41Packet(++sequenceId, getColumnFieldDetailFlag(each), each.getSchema(), each.getTable(), each.getTable(),
                    each.getColumnLabel(), each.getColumnName(), each.getColumnLength(), MySQLColumnType.valueOfJDBCType(each.getColumnType()), each.getDecimals()));
        }
        result.add(new MySQLEofPacket(++sequenceId));
        return result;
    }
    
    private static int getColumnFieldDetailFlag(final QueryHeader header) {
        int result = 0;
        if (header.isPrimaryKey()) {
            result += MySQLColumnFieldDetailFlag.PRIMARY_KEY.getValue();
        }
        if (header.isNotNull()) {
            result += MySQLColumnFieldDetailFlag.NOT_NULL.getValue();
        }
        if (!header.isSigned()) {
            result += MySQLColumnFieldDetailFlag.UNSIGNED.getValue();
        }
        if (header.isAutoIncrement()) {
            result += MySQLColumnFieldDetailFlag.AUTO_INCREMENT.getValue();
        }
        return result;
    }
    
    /**
     * Build update response packets.
     * 
     * @param updateResponse update response
     * @return update response packets
     */
    public static Collection<DatabasePacket<?>> buildUpdateResponsePackets(final UpdateResponse updateResponse) {
        return Collections.singletonList(new MySQLOKPacket(1, updateResponse.getUpdateCount(), updateResponse.getLastInsertId()));
    }
}
