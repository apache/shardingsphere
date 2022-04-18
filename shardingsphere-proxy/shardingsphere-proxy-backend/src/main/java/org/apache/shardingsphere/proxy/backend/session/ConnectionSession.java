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
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementSchemaHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.sql.common.constant.TransactionIsolationLevel;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connection session.
 */
@Getter
@Setter
public final class ConnectionSession {
    
    private final DatabaseType databaseType;
    
    @Setter(AccessLevel.NONE)
    private volatile String schemaName;
    
    private volatile int connectionId;
    
    private volatile Grantee grantee;
    
    private final TransactionStatus transactionStatus;
    
    private final AttributeMap attributeMap;
    
    @Getter(AccessLevel.NONE)
    private final AtomicBoolean autoCommit = new AtomicBoolean(true);

    @Getter(AccessLevel.NONE)
    private AtomicBoolean readOnly = new AtomicBoolean(false);

    private TransactionIsolationLevel defaultIsolationLevel;

    private TransactionIsolationLevel isolationLevel;

    private final BackendConnection backendConnection;
    
    private final ExecutorStatementManager statementManager;
    
    public ConnectionSession(final DatabaseType databaseType, final TransactionType initialTransactionType, final AttributeMap attributeMap) {
        this.databaseType = databaseType;
        transactionStatus = new TransactionStatus(initialTransactionType);
        this.attributeMap = attributeMap;
        backendConnection = determineBackendConnection();
        statementManager = determineStatementManager();
    }
    
    private BackendConnection determineBackendConnection() {
        String proxyBackendDriverType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.PROXY_BACKEND_DRIVER_TYPE);
        return "ExperimentalVertx".equals(proxyBackendDriverType) ? new VertxBackendConnection(this) : new JDBCBackendConnection(this);
    }
    
    private ExecutorStatementManager determineStatementManager() {
        String proxyBackendDriverType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.PROXY_BACKEND_DRIVER_TYPE);
        return "ExperimentalVertx".equals(proxyBackendDriverType) ? new VertxBackendStatement() : new JDBCBackendStatement();
    }
    
    /**
     * Change schema of current channel.
     *
     * @param schemaName schema name
     */
    public void setCurrentSchema(final String schemaName) {
        if (null != schemaName && schemaName.equals(this.schemaName)) {
            return;
        }
        if (transactionStatus.isInTransaction()) {
            throw new ShardingSphereException("Failed to switch schema, please terminate current transaction.");
        }
        if (statementManager instanceof JDBCBackendStatement) {
            ((JDBCBackendStatement) statementManager).setSchemaName(schemaName);
        }
        this.schemaName = schemaName;
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public String getSchemaName() {
        return null == SQLStatementSchemaHolder.get() ? schemaName : SQLStatementSchemaHolder.get();
    }
    
    /**
     * Get default schema name.
     *
     * @return default schema name
     */
    public String getDefaultSchemaName() {
        return schemaName;
    }
    
    /**
     * Is autocommit.
     *
     * @return is autocommit
     */
    public boolean isAutoCommit() {
        return autoCommit.get();
    }

    /**
     * Set autocommit.
     *
     * @param autoCommit autocommit
     */
    public void setAutoCommit(final boolean autoCommit) {
        this.autoCommit.set(autoCommit);
    }

    /**
     * Is readonly.
     *
     * @return is readonly
     */
    public boolean isReadOnly() {
        return readOnly.get();
    }

    /**
     * Set readonly.
     *
     * @param readOnly readonly
     */
    public void setReadOnly(final boolean readOnly) {
        this.readOnly.set(readOnly);
    }
}
