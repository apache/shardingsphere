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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;

import java.util.Collection;
import java.util.LinkedList;
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
        if (Strings.isNullOrEmpty(name) || !contextManager.getMetaDataContexts().getMetaData().containsDatabase(name)) {
            throw new NoDatabaseSelectedException();
        }
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
     * Get state context.
     * 
     * @return state context
     */
    public Optional<StateContext> getStateContext() {
        return null == contextManager.getInstanceContext() ? Optional.empty() : Optional.ofNullable(contextManager.getInstanceContext().getInstance().getState());
    }
    
    /**
     * Get rules.
     * 
     * @param databaseName database name
     * @return rules
     */
    // TODO performance enhancement: cache when call init() and pay attention for refresh of rule modification
    public Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        if (!Strings.isNullOrEmpty(databaseName) && databaseExists(databaseName)) {
            result.addAll(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
        result.addAll(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
}
