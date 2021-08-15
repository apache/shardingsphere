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
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.manager.impl.MemoryContextManager;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Proxy context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyContext {
    
    private static final ProxyContext INSTANCE = new ProxyContext();
    
    private final JDBCBackendDataSource backendDataSource = new JDBCBackendDataSource();
    
    private volatile ContextManager contextManager = new MemoryContextManager();
    
    /**
     * Get instance of proxy schema schemas.
     *
     * @return instance of ShardingSphere schemas.
     */
    public static ProxyContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy context.
     *
     * @param contextManager context manager
     */
    public void init(final ContextManager contextManager) {
        this.contextManager = contextManager;
    }
    
    /**
     * Check schema exists.
     *
     * @param schemaName schema name
     * @return schema exists or not
     */
    public boolean schemaExists(final String schemaName) {
        return contextManager.getMetaDataContexts().getAllSchemaNames().contains(schemaName);
    }
    
    /**
     * Get ShardingSphere meta data.
     *
     * @param schemaName schema name
     * @return ShardingSphere meta data
     */
    public ShardingSphereMetaData getMetaData(final String schemaName) {
        if (Strings.isNullOrEmpty(schemaName) || !contextManager.getMetaDataContexts().getAllSchemaNames().contains(schemaName)) {
            throw new NoDatabaseSelectedException();
        }
        return contextManager.getMetaDataContexts().getMetaData(schemaName);
    }
    
    /**
     * Get all schema names.
     *
     * @return all schema names
     */
    public List<String> getAllSchemaNames() {
        return new ArrayList<>(contextManager.getMetaDataContexts().getAllSchemaNames());
    }
    
    /**
     * Get lock.
     * 
     * @return lock
     */
    public Optional<ShardingSphereLock> getLock() {
        return contextManager.getLock();
    }
    
    /**
     * Get state context.
     * 
     * @return state context
     */
    public StateContext getStateContext() {
        return contextManager.getMetaDataContexts().getStateContext();
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
        if (!Strings.isNullOrEmpty(databaseName) && schemaExists(databaseName)) {
            result.addAll(contextManager.getMetaDataContexts().getMetaData(databaseName).getRuleMetaData().getRules());
        }
        result.addAll(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules());
        return result;
    }
}
