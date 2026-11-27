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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.sql.SQLReceivedPacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;

/**
 * Firebird execute immediate packet without input or output messages.
 */
public final class FirebirdExecuteImmediatePacket extends FirebirdCommandPacket implements SQLReceivedPacket {
    
    @Getter
    private final int transactionId;
    
    @Getter
    private final HintValueContext hintValueContext;
    
    private final String sql;
    
    @HighFrequencyInvocation
    public FirebirdExecuteImmediatePacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(Integer.BYTES);
        transactionId = payload.readInt4();
        payload.skipReserved(2 * Integer.BYTES);
        String originSQL = payload.readString();
        hintValueContext = SQLHintUtils.extractHint(originSQL);
        sql = SQLHintUtils.removeHint(originSQL);
        payload.readBuffer();
        payload.skipReserved(Integer.BYTES);
    }
    
    @Override
    public String getSQL() {
        return sql;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
}
