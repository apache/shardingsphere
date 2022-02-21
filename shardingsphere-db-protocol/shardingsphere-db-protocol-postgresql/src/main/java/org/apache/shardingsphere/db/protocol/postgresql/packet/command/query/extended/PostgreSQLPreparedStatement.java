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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLParameterDescriptionPacket;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.List;
import java.util.Optional;

/**
 * Prepared statement for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class PostgreSQLPreparedStatement {
    
    private final String sql;
    
    private final SQLStatement sqlStatement;
    
    private final List<PostgreSQLColumnType> parameterTypes;
    
    @Getter(AccessLevel.NONE)
    private PostgreSQLPacket rowDescription;
    
    /**
     * Describe parameters of the prepared statement.
     *
     * @return packet of parameter descriptions
     */
    public PostgreSQLParameterDescriptionPacket describeParameters() {
        return new PostgreSQLParameterDescriptionPacket(parameterTypes);
    }
    
    /**
     * Describe rows of the prepared statement.
     * 
     * @return packet of row description
     */
    public Optional<PostgreSQLPacket> describeRows() {
        return Optional.ofNullable(rowDescription);
    }
}
