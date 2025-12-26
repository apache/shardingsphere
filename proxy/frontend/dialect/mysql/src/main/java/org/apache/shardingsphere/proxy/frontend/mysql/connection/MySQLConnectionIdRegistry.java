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

package org.apache.shardingsphere.proxy.frontend.mysql.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MySQL connection ID registry.
 * 
 * <p>
 * Maps MySQL handshake connection IDs (32-bit values from the protocol handshake)
 * to process IDs (cluster-wide unique identifiers used in KILL QUERY).
 * This maintains the stable mapping across the lifecycle of a connection,
 * from handshake through execution to completion.
 * </p>
 * 
 * <h2>Lifecycle Overview</h2>
 * <ol>
 *   <li><b>Handshake (MySQLAuthenticationEngine.handshake):</b> 
 *       MySQL connection ID generated (32-bit, unique within proxy instance).
 *       Sent to client in MySQLHandshakePacket.</li>
 *   <li><b>Authentication (FrontendChannelInboundHandler.authenticate):</b>
 *       After authentication succeeds, processEngine.connect() generates cluster-unique processId.
 *       ConnectionSession.setProcessId() stores it (happens BEFORE first command).</li>
 *   <li><b>Command Execution (MySQLCommandExecuteEngine.getCommandExecutor):</b>
 *       During first command, registry.register() binds MySQL connectionId → processId.
 *       This enables correct routing of KILL commands in cluster mode.</li>
 *   <li><b>Kill Query (MySQLKillProcessExecutor):</b>
 *       Client sends KILL &lt;processId&gt;; ProcessService.killProcess() routes using cluster-unique processId.
 *       No need to use MySQLConnectionIdRegistry for KILL (uses processId directly).</li>
 *   <li><b>Connection Close (MySQLFrontendEngine.release):</b>
 *       registry.unregister() cleans up mapping to prevent memory leaks in singleton.</li>
 * </ol>
 * 
 * <h2>Thread Safety</h2>
 * <p>Uses ConcurrentHashMap for all operations; thread-safe under concurrent connections.</p>
 * 
 * <h2>Key Design Principle</h2>
 * <p>
 * This registry is MySQL-protocol-specific and isolated in the protocol layer (not in infra).
 * The infra-level Process/ProcessRegistry remains protocol-agnostic, using cluster-unique
 * processId keys. This prevents MySQL-specific details from leaking into shared infrastructure.
 * </p>
 * 
 * <h2>ProcessId Availability Timeline</h2>
 * <ul>
 *   <li>MySQL handshake connection ID: Available after handshake() returns (line 83 in MySQLFrontendEngine)</li>
 *   <li>Cluster-unique processId: Available after authenticate() succeeds (line 83 in FrontendChannelInboundHandler)</li>
 *   <li>Binding in registry: Happens on first command execution (line 69 in MySQLCommandExecuteEngine)</li>
 *   <li>By design, processId is always available before register() is called (null check at line 68 is defensive)</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLConnectionIdRegistry {
    
    private static final MySQLConnectionIdRegistry INSTANCE = new MySQLConnectionIdRegistry();
    
    private final Map<Long, String> connectionIdToProcessId = new ConcurrentHashMap<>();
    
    /**
     * Get instance.
     *
     * @return registry instance
     */
    public static MySQLConnectionIdRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register mapping from MySQL handshake connection ID to process ID.
     *
     * <p>
     * Called when a command is executed to bind the protocol-layer connection ID
     * to the cluster-unique process ID. This allows KILL queries to route correctly
     * in distributed mode by using the cluster-unique processId.
     * </p>
     *
     * @param mysqlConnectionId MySQL handshake connection ID (32-bit value, unique per proxy instance)
     * @param processId cluster-wide unique process ID (used for routing KILL commands)
     */
    public void register(final long mysqlConnectionId, final String processId) {
        if (null != processId) {
            connectionIdToProcessId.put(mysqlConnectionId, processId);
        }
    }
    
    /**
     * Unregister mapping when connection closes.
     *
     * <p>
     * Must be called during connection cleanup to prevent memory leaks in the singleton registry.
     * Called from MySQLFrontendEngine.release() when the connection session ends.
     * </p>
     *
     * @param mysqlConnectionId MySQL handshake connection ID
     */
    public void unregister(final long mysqlConnectionId) {
        connectionIdToProcessId.remove(mysqlConnectionId);
    }
    
    /**
     * Get process ID by MySQL handshake connection ID.
     *
     * <p>
     * Used during KILL query processing to map the protocol-level connection ID
     * to the cluster-unique process ID. Returns null if no mapping exists.
     * </p>
     *
     * @param mysqlConnectionId MySQL handshake connection ID
     * @return cluster-unique process ID if mapping exists, null otherwise
     */
    public String getProcessId(final long mysqlConnectionId) {
        return connectionIdToProcessId.get(mysqlConnectionId);
    }
}
