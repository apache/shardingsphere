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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.List;

@Getter
public final class PostgreSQLAggregatedCommandPacket extends PostgreSQLCommandPacket {
    
    private final List<PostgreSQLCommandPacket> packets;
    
    private final boolean containsBatchedStatements;
    
    private final int batchPacketBeginIndex;
    
    private final int batchPacketEndIndex;
    
    public PostgreSQLAggregatedCommandPacket(final List<PostgreSQLCommandPacket> packets) {
        this.packets = packets;
        String firstStatementId = null;
        String firstPortal = null;
        int parsePacketCount = 0;
        int bindPacketCountForFirstStatement = 0;
        int executePacketCountForFirstStatement = 0;
        int batchPacketBeginIndex = -1;
        int batchPacketEndIndex = -1;
        int index = 0;
        for (PostgreSQLCommandPacket each : packets) {
            if (each instanceof PostgreSQLComParsePacket) {
                if (++parsePacketCount > 1) {
                    break;
                }
                if (null == firstStatementId) {
                    firstStatementId = ((PostgreSQLComParsePacket) each).getStatementId();
                } else if (!firstStatementId.equals(((PostgreSQLComParsePacket) each).getStatementId())) {
                    break;
                }
            }
            if (each instanceof PostgreSQLComBindPacket) {
                if (-1 == batchPacketBeginIndex) {
                    batchPacketBeginIndex = index;
                }
                if (null == firstStatementId) {
                    firstStatementId = ((PostgreSQLComBindPacket) each).getStatementId();
                } else if (!firstStatementId.equals(((PostgreSQLComBindPacket) each).getStatementId())) {
                    break;
                }
                if (null == firstPortal) {
                    firstPortal = ((PostgreSQLComBindPacket) each).getPortal();
                } else if (!firstPortal.equals(((PostgreSQLComBindPacket) each).getPortal())) {
                    break;
                }
                bindPacketCountForFirstStatement++;
            }
            if (each instanceof PostgreSQLComExecutePacket) {
                if (index > batchPacketEndIndex) {
                    batchPacketEndIndex = index;
                }
                if (null == firstPortal) {
                    firstPortal = ((PostgreSQLComExecutePacket) each).getPortal();
                } else if (!firstPortal.equals(((PostgreSQLComExecutePacket) each).getPortal())) {
                    break;
                }
                executePacketCountForFirstStatement++;
            }
            index++;
        }
        this.batchPacketBeginIndex = batchPacketBeginIndex;
        this.batchPacketEndIndex = batchPacketEndIndex;
        containsBatchedStatements = bindPacketCountForFirstStatement == executePacketCountForFirstStatement && bindPacketCountForFirstStatement >= 3;
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return () -> '?';
    }
}
