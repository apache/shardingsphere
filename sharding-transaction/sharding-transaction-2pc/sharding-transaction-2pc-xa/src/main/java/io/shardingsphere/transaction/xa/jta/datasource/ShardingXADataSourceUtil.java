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

package io.shardingsphere.transaction.xa.jta.datasource;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.XADataSource;
import java.util.Properties;

/**
 * Sharding XA data source util.
 *
 * @author zhaojun
 */
public class ShardingXADataSourceUtil {
    
    /**
     * Create sharding XA data source.
     * @param databaseType database type
     * @param dataSourceName data source name
     * @param dataSourceParameter data source parameter
     * @return sharding XA data source
     * @throws PropertyException property exception
     */
    public static ShardingXADataSource createShardingXADataSource(
        final DatabaseType databaseType, final String dataSourceName, final DataSourceParameter dataSourceParameter) throws PropertyException {
        XADataSource xaDataSource = XADataSourceFactory.build(databaseType);
        Properties xaProperties = XAPropertiesFactory.createXAProperties(databaseType).build(dataSourceParameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        return new ShardingXADataSource(dataSourceName, xaDataSource);
    }
}
