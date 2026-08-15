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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
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
    
    private final List<PostgreSQLBinaryColumnType> parameterTypes;
    
    private final List<PostgreSQLPreparedStatementParameterType> parameterTypeStates = new ArrayList<>();
    
    private final List<Integer> actualParameterMarkerIndexes;
    
    @Getter(AccessLevel.NONE)
    private PostgreSQLPacket rowDescription;
    
    /**
     * Describe parameters of the prepared statement.
     *
     * @return packet of parameter descriptions
     */
    public PostgreSQLParameterDescriptionPacket describeParameters() {
        return new PostgreSQLParameterDescriptionPacket(getParameterTypeOIDs());
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
     * Get parameter type states.
     *
     * @return parameter type states
     */
    public List<PostgreSQLPreparedStatementParameterType> getParameterTypeStates() {
        if (parameterTypeStates.isEmpty()) {
            for (PostgreSQLBinaryColumnType each : parameterTypes) {
                parameterTypeStates.add(PostgreSQLPreparedStatementParameterType.valueOf(each));
            }
        }
        return parameterTypeStates;
    }
    
    /**
     * Set parameter type state.
     *
     * @param paramIndex parameter index
     * @param parameterType parameter type state
     */
    public void setParameterType(final int paramIndex, final PostgreSQLPreparedStatementParameterType parameterType) {
        getParameterTypeStates().set(paramIndex, parameterType);
        parameterTypes.set(paramIndex, parameterType.getProtocolType());
    }
    
    /**
     * Check whether all parameter types are resolved.
     *
     * @return whether all parameter types are resolved
     */
    public boolean isParameterTypesResolved() {
        for (PostgreSQLPreparedStatementParameterType each : getParameterTypeStates()) {
            if (!each.isResolved()) {
                return false;
            }
        }
        return true;
    }
    
    private List<Integer> getParameterTypeOIDs() {
        List<Integer> result = new ArrayList<>(getParameterTypeStates().size());
        for (PostgreSQLPreparedStatementParameterType each : getParameterTypeStates()) {
            result.add(each.getWireOID());
        }
        return result;
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
