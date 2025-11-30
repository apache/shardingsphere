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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Text result set row packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_row.html">Text Resultset Row</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLTextResultSetRowPacket extends MySQLPacket {
    
    private static final int NULL = 0xfb;
    
    private final Collection<Object> data;
    
    public MySQLTextResultSetRowPacket(final MySQLPacketPayload payload, final int columnCount) {
        data = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            data.add(payload.readStringLenenc());
        }
    }
    
    @Override
    protected void write(final MySQLPacketPayload payload) {
        for (Object each : data) {
            if (null == each) {
                payload.writeInt1(NULL);
                continue;
            }
            writeDataIntoPayload(payload, each);
        }
    }
    
    private void writeDataIntoPayload(final MySQLPacketPayload payload, final Object data) {
        if (data instanceof byte[]) {
            payload.writeBytesLenenc((byte[]) data);
        } else if (data instanceof Timestamp && 0 == ((Timestamp) data).getNanos()) {
            payload.writeStringLenenc(data.toString().split("\\.")[0]);
        } else if (data instanceof BigDecimal) {
            payload.writeStringLenenc(((BigDecimal) data).toPlainString());
        } else if (data instanceof Boolean) {
            payload.writeBytesLenenc((boolean) data ? new byte[]{1} : new byte[]{0});
        } else if (data instanceof LocalDateTime) {
            payload.writeStringLenenc(DateTimeFormatterFactory.getDatetimeFormatter().format((LocalDateTime) data));
        } else {
            payload.writeStringLenenc(data.toString());
        }
    }
}
