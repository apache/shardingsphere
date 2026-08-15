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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.List;

/**
 * Parameter description packet for PostgreSQL.
 */
public final class PostgreSQLParameterDescriptionPacket extends PostgreSQLIdentifierPacket {
    
    private final List<Integer> parameterTypeOIDs;
    
    public PostgreSQLParameterDescriptionPacket(final List<?> parameterTypes) {
        parameterTypeOIDs = new java.util.ArrayList<>(parameterTypes.size());
        for (Object each : parameterTypes) {
            parameterTypeOIDs.add(each instanceof PostgreSQLBinaryColumnType ? ((PostgreSQLBinaryColumnType) each).getValue() : (Integer) each);
        }
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt2(parameterTypeOIDs.size());
        for (Integer each : parameterTypeOIDs) {
            payload.writeInt4(each);
        }
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.PARAMETER_DESCRIPTION;
    }
}
