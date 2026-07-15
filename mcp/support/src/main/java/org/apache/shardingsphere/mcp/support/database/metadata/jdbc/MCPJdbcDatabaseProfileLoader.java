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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * MCP JDBC database profile loader.
 */
public final class MCPJdbcDatabaseProfileLoader {
    
    /**
     * Load runtime database profiles.
     *
     * @param runtimeDatabases runtime database configurations
     * @return runtime database profiles
     */
    public Map<String, RuntimeDatabaseProfile> load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, RuntimeDatabaseProfile> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            result.put(entry.getKey(), load(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    /**
     * Load runtime database profile.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @return runtime database profile
     * @throws RuntimeDatabaseConnectionException when runtime database profile loading fails
     */
    public RuntimeDatabaseProfile load(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        DatabaseType databaseType;
        String databaseVersion;
        TransactionCapability transactionCapability;
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            databaseType = loadDatabaseType(databaseName, databaseMetaData);
            databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
            transactionCapability = loadTransactionCapability(databaseMetaData);
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, ex);
        }
        return new RuntimeDatabaseProfile(databaseName, databaseType.getType(), databaseVersion, transactionCapability,
                resolveIdentifierCasePolicySet(databaseName, databaseType, runtimeDatabaseConfig));
    }
    
    private TransactionCapability loadTransactionCapability(final DatabaseMetaData databaseMetaData) throws SQLException {
        if (!databaseMetaData.supportsTransactions()) {
            return TransactionCapability.NONE;
        }
        return databaseMetaData.supportsSavepoints() ? TransactionCapability.LOCAL_WITH_SAVEPOINT : TransactionCapability.LOCAL;
    }
    
    private IdentifierCasePolicySet resolveIdentifierCasePolicySet(final String databaseName, final DatabaseType databaseType,
                                                                   final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        return new IdentifierCasePolicyResolver().resolve(databaseType, new ConfigurationProperties(new Properties()),
                new RuntimeDatabaseDataSource(databaseName, runtimeDatabaseConfig));
    }
    
    private DatabaseType loadDatabaseType(final String databaseName, final DatabaseMetaData databaseMetaData) throws SQLException {
        try {
            return DatabaseTypeFactory.get(databaseMetaData);
        } catch (final ShardingSphereExternalException ex) {
            throw RuntimeDatabaseConnectionException.invalidConfiguration(databaseName, ex);
        }
    }
    
    @RequiredArgsConstructor
    private static final class RuntimeDatabaseDataSource implements DataSource {
        
        private final String databaseName;
        
        private final RuntimeDatabaseConfiguration runtimeDatabaseConfig;
        
        @Override
        public Connection getConnection() throws SQLException {
            try {
                return runtimeDatabaseConfig.openConnection(databaseName);
            } catch (final RuntimeDatabaseConnectionException ex) {
                if (ex.getCause() instanceof SQLException cause) {
                    throw cause;
                }
                throw new SQLException(ex);
            }
        }
        
        @Override
        public Connection getConnection(final String username, final String password) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public void setLogWriter(final PrintWriter out) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public void setLoginTimeout(final int seconds) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException(String.format("Unable to unwrap runtime database data source to `%s`.", iface.getName()));
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) {
            return iface.isInstance(this);
        }
    }
}
