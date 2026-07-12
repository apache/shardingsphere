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

package org.apache.shardingsphere.mcp.support.database.capability;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * MCP database capability provider.
 */
public final class MCPDatabaseCapabilityProvider implements MCPFeatureCapabilityFacade {
    
    private final Map<String, RuntimeDatabaseProfile> databaseProfiles;
    
    private final Map<String, MCPDatabaseCapability> databaseCapabilities;
    
    public MCPDatabaseCapabilityProvider(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        databaseProfiles = new MCPJdbcDatabaseProfileLoader().load(runtimeDatabases);
        databaseCapabilities = createDatabaseCapabilities(databaseProfiles, runtimeDatabases);
    }
    
    @Override
    public Optional<MCPDatabaseCapability> provide(final String databaseName) {
        return Optional.ofNullable(databaseCapabilities.get(databaseName));
    }
    
    @Override
    public Optional<RuntimeDatabaseProfile> findDatabaseProfile(final String databaseName) {
        return Optional.ofNullable(databaseProfiles.get(databaseName));
    }
    
    @Override
    public List<RuntimeDatabaseProfile> getDatabaseProfiles() {
        return new LinkedList<>(databaseProfiles.values());
    }
    
    private Map<String, MCPDatabaseCapability> createDatabaseCapabilities(final Map<String, RuntimeDatabaseProfile> databaseProfiles,
                                                                          final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, MCPDatabaseCapability> result = new LinkedHashMap<>(databaseProfiles.size(), 1F);
        for (RuntimeDatabaseProfile each : databaseProfiles.values()) {
            TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, each.getDatabaseType())
                    .ifPresent(option -> result.put(each.getDatabase(), new MCPDatabaseCapability(each.getDatabase(), each.isSupportsTransaction(), each.isSupportsSavepoint(),
                            resolveIdentifierCasePolicySet(each, runtimeDatabases.get(each.getDatabase())), option)));
        }
        return result;
    }
    
    private IdentifierCasePolicySet resolveIdentifierCasePolicySet(final RuntimeDatabaseProfile databaseProfile, final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseProfile.getDatabaseType());
        return new IdentifierCasePolicyResolver().resolve(databaseType, new ConfigurationProperties(new Properties()),
                new RuntimeDatabaseDataSource(databaseProfile.getDatabase(), runtimeDatabaseConfig));
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
