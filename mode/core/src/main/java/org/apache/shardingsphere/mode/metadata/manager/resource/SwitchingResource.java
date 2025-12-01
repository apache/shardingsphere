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

package org.apache.shardingsphere.mode.metadata.manager.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Switching resource.
 */
@RequiredArgsConstructor
@Getter
public final class SwitchingResource {
    
    private final Map<StorageNode, DataSource> newDataSources;
    
    private final Map<StorageNode, DataSource> staleDataSources;
    
    private final Collection<String> staleStorageUnitNames;
    
    private final Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap;
    
    /**
     * Close stale data sources.
     */
    public void closeStaleDataSources() {
        staleDataSources.values().stream().filter(Objects::nonNull).forEach(each -> new DataSourcePoolDestroyer(each).asyncDestroy());
    }
}
