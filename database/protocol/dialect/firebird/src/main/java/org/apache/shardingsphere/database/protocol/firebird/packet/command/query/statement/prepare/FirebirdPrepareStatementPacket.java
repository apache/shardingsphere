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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.sql.SQLReceivedPacket;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird allocate statement packet.
 */
@Getter
public final class FirebirdPrepareStatementPacket extends FirebirdCommandPacket implements SQLReceivedPacket {
    
    private final int transactionId;
    
    private final int statementId;
    
    private final int sqlDialect;
    
    private final HintValueContext hintValueContext;
    
    private final String sql;
    
    private final List<FirebirdSQLInfoPacketType> infoItems = new ArrayList<>();
    
    private int currentItemIdx = -1;
    
    private final int maxLength;
    
    public FirebirdPrepareStatementPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        transactionId = payload.readInt4();
        statementId = payload.readInt4();
        sqlDialect = payload.readInt4();
        String originSQL = payload.readString();
        hintValueContext = SQLHintUtils.extractHint(originSQL);
        sql = SQLHintUtils.removeHint(originSQL);
        parseInfo(payload.readBuffer());
        maxLength = payload.readInt4();
    }
    
    private void parseInfo(final ByteBuf buffer) {
        while (buffer.isReadable()) {
            infoItems.add(FirebirdSQLInfoPacketType.valueOf(buffer.readByte()));
        }
    }
    
    public boolean isValidStatementHandle() {
        return statementId != 0xFFFF;
    }
    
    /**
     * Move to the next info item.
     *
     * @return {@code true} if there is a next item, {@code false} otherwise
     */
    public boolean nextItem() {
        ++currentItemIdx;
        return currentItemIdx < infoItems.size();
    }
    
    public FirebirdSQLInfoPacketType getCurrentItem() {
        return infoItems.get(currentItemIdx);
    }
    
    @Override
    public String getSQL() {
        return sql;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @return Length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        int length = 16;
        length += payload.getBufferLength(length);
        length += payload.getBufferLength(length);
        return length + 4;
    }
}
