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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.List;

/**
 * Data row packet for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public final class PostgreSQLDataRowPacket implements PostgreSQLPacket {
    
    private final char messageType = PostgreSQLCommandPacketType.DATA_ROW.getValue();
    
    private final List<Object> data;
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt2(data.size());
        for (Object each : data) {
            if (null == each) {
                payload.writeInt4(0xFFFFFFFF);
            } else {
                if (each instanceof byte[]) {
                    payload.writeInt4(((byte[]) each).length);
                    payload.writeBytes((byte[]) each);
                } else if (each instanceof SQLXML) {
                    writeSQLXMLData(payload, each);
                } else {
                    String columnData = each.toString();
                    payload.writeInt4(columnData.getBytes().length);
                    payload.writeStringEOF(columnData);
                }
            }
        }
    }
    
    private void writeSQLXMLData(final PostgreSQLPacketPayload payload, final Object data) {
        try {
            payload.writeInt4(((SQLXML) data).getString().getBytes().length);
            payload.writeStringEOF(((SQLXML) data).getString());
        } catch (final SQLException ex) {
            log.error("PostgreSQL DataRowPacket write SQLXML type exception", ex);
        }
    }
}
