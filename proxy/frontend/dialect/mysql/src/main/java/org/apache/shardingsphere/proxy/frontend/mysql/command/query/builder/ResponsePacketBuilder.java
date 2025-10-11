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
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Response packet builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponsePacketBuilder {
    
    private static final String BINARY_COLUMN_TYPE_KEYWORD = "BINARY";
    
    private static final String BLOB_COLUMN_TYPE_KEYWORD = "BLOB";
    
    private static final Collection<Integer> BINARY_TYPES = new HashSet<>(Arrays.asList(Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY));
    
    /**
     * Build query response packets.
     *
     * @param queryResponseHeader query response header
     * @param sessionCharacterSet MySQL character set id
     * @param statusFlags server status flags
     * @return query response packets
     */
    public static Collection<DatabasePacket> buildQueryResponsePackets(final QueryResponseHeader queryResponseHeader, final int sessionCharacterSet, final int statusFlags) {
        Collection<DatabasePacket> result = new LinkedList<>();
        List<QueryHeader> queryHeaders = queryResponseHeader.getQueryHeaders();
        result.add(new MySQLFieldCountPacket(queryHeaders.size()));
        for (QueryHeader each : queryHeaders) {
            int characterSet = BINARY_TYPES.contains(each.getColumnType()) ? MySQLCharacterSets.BINARY.getId() : sessionCharacterSet;
            result.add(new MySQLColumnDefinition41Packet(characterSet, getColumnDefinitionFlag(each), each.getSchema(), each.getTable(), each.getTable(),
                    each.getColumnLabel(), each.getColumnName(), each.getColumnLength(), MySQLBinaryColumnType.valueOfJDBCType(each.getColumnType()), each.getDecimals(), false));
        }
        result.add(new MySQLEofPacket(statusFlags));
        return result;
    }
    
    private static int getColumnDefinitionFlag(final QueryHeader header) {
        int result = 0;
        if (header.isPrimaryKey()) {
            result += MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue();
        }
        if (header.isNotNull()) {
            result += MySQLColumnDefinitionFlag.NOT_NULL.getValue();
        }
        if (!header.isSigned()) {
            result += MySQLColumnDefinitionFlag.UNSIGNED.getValue();
        }
        if (header.isAutoIncrement()) {
            result += MySQLColumnDefinitionFlag.AUTO_INCREMENT.getValue();
        }
        if (header.getColumnTypeName().contains(BINARY_COLUMN_TYPE_KEYWORD) || header.getColumnTypeName().contains(BLOB_COLUMN_TYPE_KEYWORD)) {
            result += MySQLColumnDefinitionFlag.BINARY_COLLATION.getValue();
        }
        if (BINARY_TYPES.contains(header.getColumnType())) {
            result += MySQLColumnDefinitionFlag.BLOB.getValue();
        }
        return result;
    }
    
    /**
     * Build update response packets.
     *
     * @param updateResponseHeader update response header
     * @param serverStatusFlag server status flag
     * @return update response packets
     */
    public static Collection<DatabasePacket> buildUpdateResponsePackets(final UpdateResponseHeader updateResponseHeader, final int serverStatusFlag) {
        return Collections.singleton(new MySQLOKPacket(updateResponseHeader.getUpdateCount(), updateResponseHeader.getLastInsertId(), serverStatusFlag));
    }
}
