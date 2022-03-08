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

package org.apache.shardingsphere.infra.metadata.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere resource.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereResource {
    
    private final Map<String, DataSource> dataSources;
    
    private final DataSourcesMetaData dataSourcesMetaData;
    
    private final CachedDatabaseMetaData cachedDatabaseMetaData;
    
    private final DatabaseType databaseType;
    
    /**
     * Get all instance data sources.
     *
     * @return all instance data sources
     */
    public Collection<DataSource> getAllInstanceDataSources() {
        return dataSources.entrySet().stream().filter(entry -> dataSourcesMetaData.getAllInstanceDataSourceNames().contains(entry.getKey())).map(Entry::getValue).collect(Collectors.toSet());
    }
    
    /**
     * Get not existed resource name.
     * 
     * @param resourceNames resource names to be judged
     * @return not existed resource names
     */
    public Collection<String> getNotExistedResources(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !dataSources.containsKey(each)).collect(Collectors.toSet());
    }
    
    /**
     * Close data source.
     *
     * @param dataSource data source to be closed
     */
    public void close(final DataSource dataSource) {
        new DataSourcePoolDestroyer(dataSource).asyncDestroy();
    }
}
