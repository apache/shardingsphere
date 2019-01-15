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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.XAConnectionFactory;
import lombok.Getter;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.SQLException;

/**
 * Sharding XA data source.
 *
 * @author zhaojun
 */
public final class ShardingXADataSource extends AbstractUnsupportedShardingXADataSource {
    
    @Getter
    private final String resourceName;
    
    @Getter
    private final XADataSource xaDataSource;
    
    private final DatabaseType databaseType;
    
    private final DataSource originalDataSource;
    
    private final boolean isOriginalXADataSource;
    
    public ShardingXADataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource) {
        this.resourceName = resourceName;
        this.databaseType = databaseType;
        originalDataSource = dataSource;
        if (dataSource instanceof XADataSource) {
            xaDataSource = (XADataSource) dataSource;
            isOriginalXADataSource = true;
        } else {
            xaDataSource = XADataSourceFactory.build(databaseType, dataSource);
            isOriginalXADataSource = false;
        }
    }
    
    @Override
    public ShardingXAConnection getXAConnection() throws SQLException {
        XAConnection xaConnection = isOriginalXADataSource ? xaDataSource.getXAConnection()
                : XAConnectionFactory.createShardingXAConnection(databaseType, xaDataSource, originalDataSource.getConnection());
        return new ShardingXAConnection(resourceName, xaConnection);
    }
}
