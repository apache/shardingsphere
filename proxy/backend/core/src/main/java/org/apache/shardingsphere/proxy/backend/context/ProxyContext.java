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

package org.apache.shardingsphere.proxy.backend.context;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.datasource.JDBCBackendDataSource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Proxy context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyContext {
    
    private static final ProxyContext INSTANCE = new ProxyContext();
    
    private final JDBCBackendDataSource backendDataSource = new JDBCBackendDataSource();
    
    private ContextManager contextManager;
    
    /**
     * Initialize proxy context.
     *
     * @param contextManager context manager
     */
    public static void init(final ContextManager contextManager) {
        INSTANCE.contextManager = contextManager;
    }
    
    /**
     * Get instance of proxy context.
     *
     * @return got instance
     */
    public static ProxyContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Check database exists.
     *
     * @param name database name
     * @return database exists or not
     */
    public boolean databaseExists(final String name) {
        return contextManager.getMetaDataContexts().getMetaData().containsDatabase(name);
    }
    
    /**
     * Get database.
     *
     * @param name database name
     * @return got database
     */
    public ShardingSphereDatabase getDatabase(final String name) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(name) && contextManager.getMetaDataContexts().getMetaData().containsDatabase(name), NoDatabaseSelectedException::new);
        return contextManager.getMetaDataContexts().getMetaData().getDatabase(name);
    }
    
    /**
     * Get all database names.
     *
     * @return all database names
     */
    public Collection<String> getAllDatabaseNames() {
        return contextManager.getMetaDataContexts().getMetaData().getDatabases().values().stream().map(ShardingSphereDatabase::getName).collect(Collectors.toList());
    }
    
    /**
     * Get instance state context.
     * 
     * @return instance state context
     */
    public Optional<InstanceStateContext> getInstanceStateContext() {
        return null == contextManager.getInstanceContext() ? Optional.empty() : Optional.ofNullable(contextManager.getInstanceContext().getInstance().getState());
    }
}
