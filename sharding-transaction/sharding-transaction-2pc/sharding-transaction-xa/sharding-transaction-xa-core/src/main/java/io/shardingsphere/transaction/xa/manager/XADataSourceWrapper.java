/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.manager;

import com.atomikos.beans.PropertyException;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;
import javax.sql.XADataSource;

/**
 * XA data source wrapper.
 *
 * @author zhaojun
 */
public interface XADataSourceWrapper {
    
    /**
     * Get a wrapper datasource pool for XA.
     *
     * @param databaseType database type
     * @param xaDataSource xa data source
     * @param dataSourceName data source name
     * @param parameter data source parameter
     * @return wrapper xa data source pool
     * @throws PropertyException property exception
     */
    DataSource wrap(DatabaseType databaseType, XADataSource xaDataSource, String dataSourceName, DataSourceParameter parameter) throws PropertyException;
}
