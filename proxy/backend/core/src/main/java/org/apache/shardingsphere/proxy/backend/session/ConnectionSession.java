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

package org.apache.shardingsphere.proxy.backend.session;

import io.netty.util.AttributeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Connection session.
 */
@Getter
@Setter
public final class ConnectionSession {
    
    private final DatabaseType protocolType;
    
    @Setter(AccessLevel.NONE)
    private volatile String currentDatabaseName;
    
    private volatile int connectionId;
    
    private final TransactionStatus transactionStatus;
    
    private final AttributeMap attributeMap;
    
    private volatile boolean autoCommit = true;
    
    private volatile boolean readOnly;
    
    private TransactionIsolationLevel defaultIsolationLevel;
    
    private TransactionIsolationLevel isolationLevel;
    
    private final ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @SuppressWarnings("rawtypes")
    private final ExecutorStatementManager statementManager;
    
    private final ServerPreparedStatementRegistry serverPreparedStatementRegistry = new ServerPreparedStatementRegistry();
    
    private final AtomicReference<ConnectionContext> connectionContext = new AtomicReference<>();
    
    private final RequiredSessionVariableRecorder requiredSessionVariableRecorder = new RequiredSessionVariableRecorder();
    
    private volatile String processId;
    
    private QueryContext queryContext;
    
    public ConnectionSession(final DatabaseType protocolType, final AttributeMap attributeMap) {
        this.protocolType = protocolType;
        transactionStatus = new TransactionStatus();
        this.attributeMap = attributeMap;
        databaseConnectionManager = new ProxyDatabaseConnectionManager(this);
        statementManager = new JDBCBackendStatement();
    }

    /**
     * Set ProcessId
     * @param  processId current processId
     */
    public void setProcessId(final String processId) {
        this.processId = processId;
    }
    public void bindProcessToConnection() {
        if (!"MySQL".equals(protocolType.getType())) {
            return;
        }
        if (null == processId || connectionId <= 0) {
            return;
        }
        Process process = ProcessRegistry.getInstance().get(processId);
        if (process != null) {
            process.setMySQLThreadId((long) connectionId);
        }
    }

    /**
     * Set grantee.
     *
     * @param grantee grantee
     */
    public void setGrantee(final Grantee grantee) {
        connectionContext.set(new ConnectionContext(databaseConnectionManager::getUsedDataSourceNames, grantee));
    }
    
    /**
     * Change database of current channel.
     *
     * @param currentDatabaseName current database name
     */
    public void setCurrentDatabaseName(final String currentDatabaseName) {
        if (null == currentDatabaseName || !currentDatabaseName.equals(this.currentDatabaseName)) {
            this.currentDatabaseName = currentDatabaseName;
            connectionContext.get().setCurrentDatabaseName(currentDatabaseName);
        }
    }
    
    /**
     * Get connection context.
     *
     * @return connection context
     */
    public ConnectionContext getConnectionContext() {
        return connectionContext.get();
    }
    
    /**
     * Get used database name.
     *
     * @return used database name
     */
    public String getUsedDatabaseName() {
        return null == queryContext || queryContext.getUsedDatabaseNames().isEmpty() ? currentDatabaseName : queryContext.getUsedDatabaseNames().iterator().next();
    }
    
    /**
     * Get isolation level.
     *
     * @return isolation level
     */
    public Optional<TransactionIsolationLevel> getIsolationLevel() {
        return Optional.ofNullable(isolationLevel);
    }
    
    /**
     * Clear query context.
     */
    public void clearQueryContext() {
        queryContext = null;
    }
}
