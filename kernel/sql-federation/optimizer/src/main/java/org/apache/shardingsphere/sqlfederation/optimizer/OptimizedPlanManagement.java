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

package org.apache.shardingsphere.sqlfederation.optimizer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.filter.FilterableSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.filter.FilterableTable;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidatorCatalogReader;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Provide cache for storing optimized plan.
 */
@Getter
@Setter
public class OptimizedPlanManagement {
    
    public static final long CAPACITY = 2000;
    
    private static final int EXPIRETIME = 3600000;
    
    private final Cache<SQLInfo, SQLOptimizeContext> cache;
    
    private final SQLOptimizeEngine optimizer;
    
    public OptimizedPlanManagement(final SQLOptimizeEngine optimizer) {
        this.cache = buildCache();
        this.optimizer = optimizer;
    }
    
    private Cache<SQLInfo, SQLOptimizeContext> buildCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(EXPIRETIME, TimeUnit.MILLISECONDS)
                .maximumSize(CAPACITY)
                .build();
    }
    
    /**
     * Get rel node from cache by sql node.
     *
     * @param sqlNode ast node
     * @param parameters sql parameters
     * @param tableNames table names
     * @param useCache whether use cache
     * @return rel node
     */
    public SQLOptimizeContext get(final SqlNode sqlNode, final Map<String, Object> parameters, final Collection<String> tableNames, final boolean useCache) {
        if (!useCache) {
            return optimizer.optimize(sqlNode);
        }
        SQLOptimizeContext result = cache.getIfPresent(new SQLInfo(sqlNode.toString(), parameters, tableNames));
        if (null == result) {
            String key = sqlNode.toString();
            result = optimizer.optimize(sqlNode);
            cache.put(new SQLInfo(key, parameters, tableNames), result);
            return result;
        }
        return result;
    }
    
    /**
     * Refresh metadata in cache.
     *
     * @param event meta data refreshed event
     */
    public void refresh(final Optional<MetaDataRefreshedEvent> event) {
        Set<String> tableNames = new HashSet<>();
        String schemaName;
        SqlValidatorCatalogReader catalogReader = optimizer.getConverter().validator.getCatalogReader();
        if (event.get() instanceof SchemaAlteredEvent) {
            SchemaAlteredEvent schemaAlteredEvent = (SchemaAlteredEvent) event.get();
            schemaName = schemaAlteredEvent.getSchemaName();
            Collection<ShardingSphereTable> alteredTables = schemaAlteredEvent.getAlteredTables();
            for (ShardingSphereTable each : alteredTables) {
                tableNames.add(each.getName());
                FilterableSchema schema = (FilterableSchema) catalogReader.getRootSchema().getSubSchemaMap().get(schemaName).schema;
                FilterableTable table = (FilterableTable) schema.getTableMap().get(each.getName());
                table.setTable(each);
            }
            Collection<String> droppedTables = schemaAlteredEvent.getDroppedTables();
            for (String each : droppedTables) {
                tableNames.add(each);
                FilterableSchema schema = (FilterableSchema) catalogReader.getRootSchema().getSubSchemaMap().get(schemaName).schema;
                schema.getTableMap().remove(each);
            }
        }
        // refresh cache
        Set<SQLInfo> cacheKeys = cache.asMap().keySet();
        for (String name : tableNames) {
            for (SQLInfo key : cacheKeys) {
                if (key.tableNames.stream().anyMatch(each -> each.equalsIgnoreCase(name))) {
                    cache.invalidate(key);
                }
            }
        }
    }
    
    @AllArgsConstructor
    private class SQLInfo {
        
        private String sqlNode;
        
        private Map<String, Object> parameters;
        
        private Collection<String> tableNames;
        
        SQLInfo(final Collection<String> tableNames) {
            this.tableNames = tableNames;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final SQLInfo sqlInfo = (SQLInfo) o;
            if ((null == sqlInfo.parameters) && (null == sqlInfo.sqlNode)) {
                tableNames.retainAll(sqlInfo.tableNames);
                return !tableNames.isEmpty();
            }
            if (sqlNode.equals(sqlInfo.sqlNode) && parameters.toString().equals(sqlInfo.parameters.toString())) {
                tableNames.retainAll(sqlInfo.tableNames);
                return !tableNames.isEmpty();
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            String sqlNodeInString = sqlNode != null ? sqlNode : "";
            String parametersInString = parameters != null ? parameters.toString() : "";
            String sqlInfoInString = "SQLInfo{" + "sqlNode=" + sqlNodeInString + ", parameters=" + parametersInString + ", tableNames=" + tableNames.toString() + '}';
            return sqlInfoInString.hashCode();
        }
    }
}
