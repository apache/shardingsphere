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
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementDatabaseHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.sql.common.constant.TransactionIsolationLevel;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Connection session.
 */
@Getter
@Setter
public final class ConnectionSession {
    
    private final DatabaseType databaseType;
    
    @Setter(AccessLevel.NONE)
    private volatile String databaseName;
    
    private volatile int connectionId;
    
    private volatile Grantee grantee;
    
    private final TransactionStatus transactionStatus;
    
    private final AttributeMap attributeMap;
    
    private volatile boolean autoCommit = true;
    
    private volatile boolean readOnly;
    
    private TransactionIsolationLevel defaultIsolationLevel;
    
    private TransactionIsolationLevel isolationLevel;
    
    private final BackendConnection backendConnection;
    
    private final ExecutorStatementManager statementManager;
    
    private final Map<String, CursorStatementContext> cursorDefinitions = new ConcurrentHashMap<>();
    
    private final PreparedStatementRegistry preparedStatementRegistry = new PreparedStatementRegistry();
    
    public ConnectionSession(final DatabaseType databaseType, final TransactionType initialTransactionType, final AttributeMap attributeMap) {
        this.databaseType = databaseType;
        transactionStatus = new TransactionStatus(initialTransactionType);
        this.attributeMap = attributeMap;
        backendConnection = determineBackendConnection();
        statementManager = determineStatementManager();
    }
    
    private BackendConnection determineBackendConnection() {
        String proxyBackendDriverType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_BACKEND_DRIVER_TYPE);
        return "ExperimentalVertx".equals(proxyBackendDriverType) ? new VertxBackendConnection(this) : new JDBCBackendConnection(this);
    }
    
    private ExecutorStatementManager determineStatementManager() {
        String proxyBackendDriverType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_BACKEND_DRIVER_TYPE);
        return "ExperimentalVertx".equals(proxyBackendDriverType) ? new VertxBackendStatement() : new JDBCBackendStatement();
    }
    
    /**
     * Change database of current channel.
     *
     * @param databaseName database name
     */
    public void setCurrentDatabase(final String databaseName) {
        if (null != databaseName && databaseName.equals(this.databaseName)) {
            return;
        }
        if (transactionStatus.isInTransaction()) {
            throw new ShardingSphereException("Failed to switch database, please terminate current transaction.");
        }
        if (statementManager instanceof JDBCBackendStatement) {
            ((JDBCBackendStatement) statementManager).setDatabaseName(databaseName);
        }
        this.databaseName = databaseName;
    }
    
    /**
     * Get database name.
     *
     * @return database name
     */
    public String getDatabaseName() {
        return null == SQLStatementDatabaseHolder.get() ? databaseName : SQLStatementDatabaseHolder.get();
    }
    
    /**
     * Get default database name.
     *
     * @return default database name
     */
    public String getDefaultDatabaseName() {
        return databaseName;
    }
}
