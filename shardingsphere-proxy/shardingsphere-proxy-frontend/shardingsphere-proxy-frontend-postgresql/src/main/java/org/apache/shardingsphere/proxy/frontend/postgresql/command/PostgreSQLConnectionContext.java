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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL connection context.
 */
@Getter
@Setter
public final class PostgreSQLConnectionContext {
    
    @Getter(AccessLevel.NONE)
    private final Map<String, PostgreSQLPortal> portals = new LinkedHashMap<>();
    
    private final Collection<CommandExecutor> pendingExecutors = new LinkedList<>();
    
    private PostgreSQLCommandPacketType currentPacketType;
    
    private boolean errorOccurred;
    
    /**
     * Create a portal.
     *
     * @param portal portal name
     * @param binaryStatement binary statement
     * @param parameters bind parameters
     * @param resultFormats result formats
     * @param backendConnection backend connection
     * @return a new portal
     * @throws SQLException SQL exception
     */
    public PostgreSQLPortal createPortal(final String portal, final PostgreSQLBinaryStatement binaryStatement, final List<Object> parameters, final List<PostgreSQLValueFormat> resultFormats,
                                         final BackendConnection backendConnection) throws SQLException {
        PostgreSQLPortal result = new PostgreSQLPortal(binaryStatement, parameters, resultFormats, backendConnection);
        portals.put(portal, result);
        return result;
    }
    
    /**
     * Get portal.
     *
     * @param portal portal name
     * @return portal
     */
    public PostgreSQLPortal getPortal(final String portal) {
        return portals.get(portal);
    }
    
    /**
     * Close portal.
     *
     * @param portal portal name
     * @throws SQLException SQL exception
     */
    public void closePortal(final String portal) throws SQLException {
        PostgreSQLPortal result = portals.remove(portal);
        if (null != result) {
            result.close();
        }
    }
    
    /**
     * Close all portals.
     * 
     * @throws SQLException SQL exception
     */
    public void closeAllPortals() throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        for (PostgreSQLPortal each : portals.values()) {
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        portals.clear();
        if (result.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("Close all portals failed.");
        result.forEach(ex::setNextException);
        throw ex;
    }
    
    /**
     * Clear context.
     */
    public void clearContext() {
        pendingExecutors.clear();
        currentPacketType = null;
        errorOccurred = false;
    }
}
