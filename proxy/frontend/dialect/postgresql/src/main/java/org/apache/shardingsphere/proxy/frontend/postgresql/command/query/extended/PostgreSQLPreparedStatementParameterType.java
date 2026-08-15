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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Parameter type state for PostgreSQL prepared statement.
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLPreparedStatementParameterType {
    
    private final PostgreSQLBinaryColumnType protocolType;
    
    private final int wireOID;
    
    private final Integer jdbcType;
    
    private final String nativeTypeName;
    
    /**
     * Create unresolved parameter type state.
     *
     * @return unresolved parameter type state
     */
    public static PostgreSQLPreparedStatementParameterType unresolved() {
        return new PostgreSQLPreparedStatementParameterType(PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.UNSPECIFIED.getValue(), null, null);
    }
    
    /**
     * Create parameter type state from protocol type.
     *
     * @param protocolType protocol type
     * @return parameter type state
     */
    public static PostgreSQLPreparedStatementParameterType valueOf(final PostgreSQLBinaryColumnType protocolType) {
        return new PostgreSQLPreparedStatementParameterType(protocolType, protocolType.getValue(), null, null);
    }
    
    /**
     * Create parameter type state from JDBC metadata.
     *
     * @param jdbcType JDBC type
     * @param nativeTypeName native type name
     * @param wireOID wire OID (can be null)
     * @return parameter type state
     */
    public static PostgreSQLPreparedStatementParameterType valueOf(final int jdbcType, final String nativeTypeName, final Integer wireOID) {
        PostgreSQLBinaryColumnType protocolType = PostgreSQLBinaryColumnType.valueOfJDBCType(jdbcType, nativeTypeName);
        int actualWireOID = wireOID != null ? wireOID : protocolType.getValue();
        return new PostgreSQLPreparedStatementParameterType(protocolType, actualWireOID, jdbcType, nativeTypeName);
    }
    
    /**
     * Check whether parameter type is resolved.
     *
     * @return whether parameter type is resolved
     */
    public boolean isResolved() {
        return PostgreSQLBinaryColumnType.UNSPECIFIED != protocolType || isNativeType();
    }
    
    /**
     * Check whether parameter type needs native identity.
     *
     * @return whether parameter type needs native identity
     */
    public boolean isNativeType() {
        return PostgreSQLBinaryColumnType.UNSPECIFIED == protocolType && Types.OTHER == Optional.ofNullable(jdbcType).orElse(Types.NULL) && null != nativeTypeName && 0 != wireOID;
    }
    
    /**
     * Decode value.
     *
     * @param value string value to decode
     * @return decoded value
     * @throws SQLWrapperException SQL wrapper exception
     */
    public Object decode(final String value) {
        if (isNativeType()) {
            try {
                PGobject result = new PGobject();
                result.setType(nativeTypeName);
                result.setValue(value);
                return result;
            } catch (final SQLException ex) {
                throw new SQLWrapperException(ex);
            }
        }
        return protocolType.getTextValueParser().parse(value);
    }
}
