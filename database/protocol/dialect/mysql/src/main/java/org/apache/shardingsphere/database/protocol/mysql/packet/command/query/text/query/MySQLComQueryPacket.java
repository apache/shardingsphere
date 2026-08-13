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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.sql.SQLReceivedPacket;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;

import java.util.Optional;

/**
 * COM_QUERY command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query.html">COM_QUERY</a>
 */
public final class MySQLComQueryPacket extends MySQLCommandPacket implements SQLReceivedPacket {
    
    private static final String BINARY_INTRODUCER = "_binary";
    
    private static final char MALFORMED_INPUT_REPLACEMENT = (char) 0xFFFD;
    
    private final String sql;
    
    private final byte[] originalSQLBytes;
    
    @Getter
    private final HintValueContext hintValueContext;
    
    public MySQLComQueryPacket(final String sql) {
        super(MySQLCommandPacketType.COM_QUERY);
        hintValueContext = SQLHintUtils.extractHint(sql);
        this.sql = SQLHintUtils.removeHint(sql);
        originalSQLBytes = null;
    }
    
    public MySQLComQueryPacket(final MySQLPacketPayload payload) {
        super(MySQLCommandPacketType.COM_QUERY);
        byte[] sqlBytes = payload.readStringEOFByBytes();
        String originSQL = new String(sqlBytes, payload.getCharset());
        hintValueContext = SQLHintUtils.extractHint(originSQL);
        sql = SQLHintUtils.removeHint(originSQL);
        originalSQLBytes = requiresBinaryLiteralInspection(originSQL) ? sqlBytes : null;
    }
    
    private static boolean requiresBinaryLiteralInspection(final String sql) {
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (MALFORMED_INPUT_REPLACEMENT == current || '_' == current && sql.regionMatches(true, i, BINARY_INTRODUCER, 0, BINARY_INTRODUCER.length())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void doWrite(final MySQLPacketPayload payload) {
        payload.writeStringEOF(sql);
    }
    
    @Override
    public String getSQL() {
        return sql;
    }
    
    /**
     * Find original SQL bytes retained for binary literal inspection.
     *
     * @return original SQL bytes when binary literal inspection is required
     */
    public Optional<byte[]> findOriginalSQLBytes() {
        return Optional.ofNullable(originalSQLBytes);
    }
}
