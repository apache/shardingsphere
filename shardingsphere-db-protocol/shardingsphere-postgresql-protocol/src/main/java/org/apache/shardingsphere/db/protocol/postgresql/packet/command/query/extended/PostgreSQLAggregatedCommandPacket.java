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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.List;
import java.util.RandomAccess;

@Getter
@ToString
public final class PostgreSQLAggregatedCommandPacket extends PostgreSQLCommandPacket {
    
    private final List<PostgreSQLCommandPacket> packets;
    
    private final boolean containsBatchedStatements;
    
    private final int firstBindIndex;
    
    private final int lastExecuteIndex;
    
    public PostgreSQLAggregatedCommandPacket(final List<PostgreSQLCommandPacket> packets) {
        this.packets = packets;
        int parseTimes = 0;
        int firstStatementBindTimes = 0;
        int firstStatementExecuteTimes = 0;
        String firstStatement = null;
        String firstPortal = null;
        int index = 0;
        int firstBindIndex = -1;
        int lastExecuteIndex = -1;
        for (PostgreSQLCommandPacket each : packets) {
            if (each instanceof PostgreSQLComParsePacket) {
                if (++parseTimes > 1) {
                    break;
                }
                if (null == firstStatement) {
                    firstStatement = ((PostgreSQLComParsePacket) each).getStatementId();
                } else if (!firstStatement.equals(((PostgreSQLComParsePacket) each).getStatementId())) {
                    break;
                }
            }
            if (each instanceof PostgreSQLComBindPacket) {
                if (-1 == firstBindIndex) {
                    firstBindIndex = index;
                }
                if (null == firstStatement) {
                    firstStatement = ((PostgreSQLComBindPacket) each).getStatementId();
                } else if (!firstStatement.equals(((PostgreSQLComBindPacket) each).getStatementId())) {
                    break;
                }
                if (null == firstPortal) {
                    firstPortal = ((PostgreSQLComBindPacket) each).getPortal();
                } else if (!firstPortal.equals(((PostgreSQLComBindPacket) each).getPortal())) {
                    break;
                }
                firstStatementBindTimes++;
            }
            if (each instanceof PostgreSQLComExecutePacket) {
                if (index > lastExecuteIndex) {
                    lastExecuteIndex = index;
                }
                if (null == firstPortal) {
                    firstPortal = ((PostgreSQLComExecutePacket) each).getPortal();
                } else if (!firstPortal.equals(((PostgreSQLComExecutePacket) each).getPortal())) {
                    break;
                }
                firstStatementExecuteTimes++;
            }
            index++;
        }
        this.firstBindIndex = firstBindIndex;
        this.lastExecuteIndex = lastExecuteIndex;
        if (this.containsBatchedStatements = firstStatementBindTimes == firstStatementExecuteTimes && firstStatementBindTimes >= 3) {
            ensureRandomAccessible(packets);
        }
    }
    
    private void ensureRandomAccessible(final List<PostgreSQLCommandPacket> packets) {
        Preconditions.checkArgument(packets instanceof RandomAccess, "Packets must be RandomAccess");
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return () -> '?';
    }
}
