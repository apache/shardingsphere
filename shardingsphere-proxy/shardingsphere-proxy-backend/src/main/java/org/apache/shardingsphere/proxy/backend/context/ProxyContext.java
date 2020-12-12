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
import lombok.Getter;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Proxy context.
 */
@Getter
public final class ProxyContext {
    
    private static final ProxyContext INSTANCE = new ProxyContext();
    
    private final JDBCBackendDataSource backendDataSource;
    
    private MetaDataContexts metaDataContexts;
    
    private TransactionContexts transactionContexts;
    
    private ProxyContext() {
        backendDataSource = new JDBCBackendDataSource();
        metaDataContexts = new StandardMetaDataContexts();
        transactionContexts = new StandardTransactionContexts();
    }
    
    /**
     * Get instance of proxy schema schemas.
     *
     * @return instance of ShardingSphere schemas.
     */
    public static ProxyContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     * @param transactionContexts transaction manager engine contexts
     */
    public void init(final MetaDataContexts metaDataContexts, final TransactionContexts transactionContexts) {
        this.metaDataContexts = metaDataContexts;
        this.transactionContexts = transactionContexts;
    }
    
    /**
     * Check schema exists.
     *
     * @param schemaName schema name
     * @return schema exists or not
     */
    public boolean schemaExists(final String schemaName) {
        return metaDataContexts.getAllSchemas().contains(schemaName);
    }
    
    /**
     * Get ShardingSphere meta data.
     *
     * @param schemaName schema name
     * @return ShardingSphere meta data
     */
    public ShardingSphereMetaData getMetaData(final String schemaName) {
        if (Strings.isNullOrEmpty(schemaName) || !metaDataContexts.getAllSchemas().contains(schemaName)) {
            throw new NoDatabaseSelectedException();
        }
        return metaDataContexts.getMetaData(schemaName);
    }
    
    /**
     * Get all schema names.
     *
     * @return all schema names
     */
    public List<String> getAllSchemaNames() {
        return new ArrayList<>(metaDataContexts.getAllSchemas());
    }
    
    /**
     * Get data source sample.
     * 
     * @return data source sample
     */
    public Optional<DataSource> getDataSourceSample() {
        List<String> schemaNames = getAllSchemaNames();
        if (schemaNames.isEmpty()) {
            return Optional.empty();
        }
        Map<String, DataSource> dataSources = getMetaData(schemaNames.get(0)).getResource().getDataSources();
        return dataSources.values().stream().findFirst();
    }
}
