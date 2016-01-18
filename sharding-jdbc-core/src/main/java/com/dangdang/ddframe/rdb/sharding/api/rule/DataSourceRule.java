/**
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

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import com.google.common.base.Preconditions;

/**
 * 数据源配置对象.
 * 
 * @author zhangliang
 */
public final class DataSourceRule {
    
    private final Map<String, DataSource> dataSourceMap;
    
    public DataSourceRule(final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkNotNull(dataSourceMap, "Must have one data source at least.");
        Preconditions.checkState(!dataSourceMap.isEmpty(), "Must have one data source at least.");
        this.dataSourceMap = dataSourceMap;
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
