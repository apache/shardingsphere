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

package org.apache.shardingsphere.proxy.backend.schema;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.schema.datasource.impl.JDBCBackendDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Proxy schema contexts.
 */
@Getter
public final class ProxySchemaContexts {
    
    private static final ProxySchemaContexts INSTANCE = new ProxySchemaContexts();
    
    private final JDBCBackendDataSource backendDataSource;
    
    private SchemaContexts schemaContexts;
    
    private TransactionContexts transactionContexts;
    
    private ProxySchemaContexts() {
        backendDataSource = new JDBCBackendDataSource();
        schemaContexts = new StandardSchemaContexts();
        transactionContexts = new StandardTransactionContexts();
    }
    
    /**
     * Get instance of proxy schema schemas.
     *
     * @return instance of ShardingSphere schemas.
     */
    public static ProxySchemaContexts getInstance() {
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
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return null != schemaContexts && schemaContexts.getSchemaContexts().containsKey(schema);
    }
    
    /**
     * Get ShardingSphere schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public SchemaContext getSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : schemaContexts.getSchemaContexts().get(schemaName);
    }
    
    /**
     * Get schema names.
     *
     * @return schema names
     */
    public List<String> getSchemaNames() {
        return new LinkedList<>(schemaContexts.getSchemaContexts().keySet());
    }
    
    /**
     * Get data source sample.
     * 
     * @return data source sample
     */
    public Optional<DataSource> getDataSourceSample() {
        List<String> schemaNames = getSchemaNames();
        if (schemaNames.isEmpty()) {
            return Optional.empty();
        }
        Map<String, DataSource> dataSources = Objects.requireNonNull(getSchema(schemaNames.get(0))).getSchema().getDataSources();
        return dataSources.values().stream().findFirst();
    }
}
