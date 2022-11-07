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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;

/**
 * COM_QUERY command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 */
@Getter
@ToString
public final class MySQLComQueryPacket extends MySQLCommandPacket {
    
    private final String sql;
    
    private final HintValueContext hintValueContext;
    
    public MySQLComQueryPacket(final String sql, final boolean sqlCommentParseEnabled) {
        super(MySQLCommandPacketType.COM_QUERY);
        hintValueContext = sqlCommentParseEnabled ? null : SQLHintUtils.extractHint(sql);
        this.sql = sqlCommentParseEnabled ? sql : SQLHintUtils.removeHint(sql);
    }
    
    public MySQLComQueryPacket(final MySQLPacketPayload payload, final boolean sqlCommentParseEnabled) {
        super(MySQLCommandPacketType.COM_QUERY);
        String originSQL = payload.readStringEOF();
        hintValueContext = sqlCommentParseEnabled ? null : SQLHintUtils.extractHint(originSQL);
        sql = sqlCommentParseEnabled ? originSQL : SQLHintUtils.removeHint(originSQL);
    }
    
    @Override
    public void doWrite(final MySQLPacketPayload payload) {
        payload.writeStringEOF(sql);
    }
}
