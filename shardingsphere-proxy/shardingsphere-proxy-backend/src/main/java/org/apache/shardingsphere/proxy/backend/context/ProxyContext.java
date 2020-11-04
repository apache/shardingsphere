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
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Proxy context.
 */
@Getter
public final class ProxyContext {
    
    private static final ProxyContext INSTANCE = new ProxyContext();
    
    private final JDBCBackendDataSource backendDataSource;
    
    private SchemaContexts schemaContexts;
    
    private TransactionContexts transactionContexts;
    
    private ProxyContext() {
        backendDataSource = new JDBCBackendDataSource();
        schemaContexts = new StandardSchemaContexts();
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
     * Initialize proxy schema contexts.
     *
     * @param schemaContexts schema contexts
     * @param transactionContexts transaction manager engine contexts
     */
    public void init(final SchemaContexts schemaContexts, final TransactionContexts transactionContexts) {
        this.schemaContexts = schemaContexts;
        this.transactionContexts = transactionContexts;
    }
    
    /**
     * Check schema exists.
     *
     * @param schemaName schema name
     * @return schema exists or not
     */
    public boolean schemaExists(final String schemaName) {
        return schemaContexts.getMetaDataMap().containsKey(schemaName);
    }
    
    /**
     * Get meta data.
     *
     * @param schemaName schema name
     * @return meta data
     */
    public ShardingSphereMetaData getMetaData(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : schemaContexts.getMetaDataMap().get(schemaName);
    }
    
    /**
     * Get all schema names.
     *
     * @return all schema names
     */
    public List<String> getAllSchemaNames() {
        return new ArrayList<>(schemaContexts.getMetaDataMap().keySet());
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
        Map<String, DataSource> dataSources = Objects.requireNonNull(getMetaData(schemaNames.get(0))).getDataSources();
        return dataSources.values().stream().findFirst();
    }
}
