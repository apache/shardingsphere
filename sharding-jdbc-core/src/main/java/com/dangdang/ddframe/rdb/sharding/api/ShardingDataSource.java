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

package com.dangdang.ddframe.rdb.sharding.api;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingConfigurationConstant;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDataSourceAdapter;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.google.common.base.Preconditions;

/**
 * 支持分片的数据源.
 * 
 * @author zhangliang
 */
public class ShardingDataSource extends AbstractDataSourceAdapter {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseMetaData databaseMetaData;
    
    private final ShardingConfiguration configuration;
    
    private final MetricsContext metricsContext;
    
    public ShardingDataSource(final ShardingRule shardingRule) {
        this(shardingRule, new Properties());
    }
    
    public ShardingDataSource(final ShardingRule shardingRule, final Properties props) {
        this.shardingRule = shardingRule;
        databaseMetaData = getDatabaseMetaData();
        configuration = new ShardingConfiguration(props);
        metricsContext = new MetricsContext(configuration.getConfig(ShardingConfigurationConstant.METRICS_ENABLE, boolean.class), 
                configuration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, long.class), 
                configuration.getConfig(ShardingConfigurationConstant.METRICS_PACKAGE_NAME, String.class));
    }
    
    private DatabaseMetaData getDatabaseMetaData() {
        String databaseProductName = null;
        DatabaseMetaData result = null;
        for (DataSource each : shardingRule.getDataSourceRule().getDataSources()) {
            String databaseProductNameInEach;
            DatabaseMetaData metaDataInEach;
            try {
                metaDataInEach = each.getConnection().getMetaData();
                databaseProductNameInEach = metaDataInEach.getDatabaseProductName();
            } catch (final SQLException ex) {
                throw new ShardingJdbcException("Can not get data source DatabaseProductName", ex);
            }
            Preconditions.checkState(null == databaseProductName || databaseProductName.equals(databaseProductNameInEach), 
                    String.format("Database type inconsistent with '%s' and '%s'", databaseProductName, databaseProductNameInEach));
            databaseProductName = databaseProductNameInEach;
            result = metaDataInEach;
        }
        return result;
    }
    
    @Override
    public ShardingConnection getConnection() throws SQLException {
        metricsContext.register();
        return new ShardingConnection(shardingRule, databaseMetaData);
    }
    
    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
}
