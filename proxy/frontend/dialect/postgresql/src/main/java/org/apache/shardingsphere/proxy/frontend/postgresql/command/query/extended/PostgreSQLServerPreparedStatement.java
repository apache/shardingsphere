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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLParameterDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Prepared statement for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class PostgreSQLServerPreparedStatement implements ServerPreparedStatement {
    
    private final String sql;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext;
    
    private final List<PostgreSQLColumnType> parameterTypes;

    // PostgreSQLServerPreparedStatement 中新增字段 & getter/setter
    private final List<String> parameterTypeNames;
    
    private final List<Integer> actualParameterMarkerIndexes;
    
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
    
    /**
     * Adjust parameters order.
     * @param parameters parameters in pg marker index order
     * @return parameters in jdbc style marker index order
     */
    public List<Object> adjustParametersOrder(final List<Object> parameters) {
        if (parameters.isEmpty()) {
            return parameters;
        }
        List<Object> result = new ArrayList<>(parameters.size());
        for (int each : actualParameterMarkerIndexes) {
            result.add(parameters.get(each));
        }
        return result;
    }
}
