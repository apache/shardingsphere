/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Data source rule configuration.
 * 
 * @author zhangliang
 */
public final class DataSourceRule {
    
    private final Map<String, DataSource> dataSourceMap;
    
    @Getter
    private final String defaultDataSourceName;
    
    public DataSourceRule(final Map<String, DataSource> dataSourceMap) {
        this(dataSourceMap, null);
    }
    
    /**
     * Constructs a data source rule with data source map and default data source.
     * 
     * @param dataSourceMap map for data source name and data source object
     * @param defaultDataSourceName default data source name, if not indicate data source name, will fetch this data source
     */
    public DataSourceRule(final Map<String, DataSource> dataSourceMap, final String defaultDataSourceName) {
        Preconditions.checkState(!dataSourceMap.isEmpty(), "Must have one data source at least.");
        this.dataSourceMap = dataSourceMap;
        if (1 == dataSourceMap.size()) {
            this.defaultDataSourceName = dataSourceMap.entrySet().iterator().next().getKey();
            return;
        }
        if (Strings.isNullOrEmpty(defaultDataSourceName)) {
            this.defaultDataSourceName = null;
            return;
        }
        Preconditions.checkState(dataSourceMap.containsKey(defaultDataSourceName), "Data source rule must include default data source.");
        this.defaultDataSourceName = defaultDataSourceName;
    }
    
    /**
     * Get data source instance.
     * 
     * @param name data source name
     * @return data source instance
     */
    public DataSource getDataSource(final String name) {
        return dataSourceMap.get(name);
    }
    
    /**
     * Get default data source instance.
     *
     * @return default data source instance
     */
    // TODO support master-slave
    public Optional<DataSource> getDefaultDataSource() {
        return Optional.fromNullable(dataSourceMap.get(defaultDataSourceName));
    }
    
    /**
     * Get all data source's names.
     * 
     * @return all data source's names
     */
    public Collection<String> getDataSourceNames() {
        return dataSourceMap.keySet();
    }
    
    /**
     * Get all data sources.
     * 
     * @return all data sources
     */
    public Collection<DataSource> getDataSources() {
        return dataSourceMap.values();
    }
}
