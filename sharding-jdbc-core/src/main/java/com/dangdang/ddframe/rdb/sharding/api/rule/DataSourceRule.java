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
 * 数据源配置对象.
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
     * 获取数据源实例.
     * 
     * @param name 数据源名称
     * @return 数据源实例
     */
    public DataSource getDataSource(final String name) {
        return dataSourceMap.get(name);
    }
    
    /**
     * 获取默认数据源实例.
     *
     * @return 默认数据源实例
     */
    // TODO getDefaultDataSource暂时不支持读写分离
    public Optional<DataSource> getDefaultDataSource() {
        return Optional.fromNullable(dataSourceMap.get(defaultDataSourceName));
    }
    
    /**
     * 获取所有数据源名称.
     * 
     * @return 所有数据源名称
     */
    public Collection<String> getDataSourceNames() {
        return dataSourceMap.keySet();
    }
    
    /**
     * 获取所有数据源.
     * 
     * @return 所有数据源
     */
    public Collection<DataSource> getDataSources() {
        return dataSourceMap.values();
    }
}
