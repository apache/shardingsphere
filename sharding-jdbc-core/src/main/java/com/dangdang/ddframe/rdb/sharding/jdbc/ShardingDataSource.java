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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.config.ShardingProperties;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDataSourceAdapter;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.router.SQLRouteEngine;
import com.google.common.base.Preconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 支持分片的数据源.
 * 
 * @author zhangliang
 */
public class ShardingDataSource extends AbstractDataSourceAdapter {
    
    private final ShardingProperties shardingProperties;
    
    private final ShardingContext shardingContext;
    
    public ShardingDataSource(final ShardingRule shardingRule) {
        this(shardingRule, new Properties());
    }
    
    public ShardingDataSource(final ShardingRule shardingRule, final Properties props) {
        Preconditions.checkNotNull(shardingRule);
        Preconditions.checkNotNull(props);
        shardingProperties = new ShardingProperties(props);
        try {
            shardingContext = new ShardingContext(shardingRule, new SQLRouteEngine(shardingRule, DatabaseType.valueFrom(getDatabaseProductName(shardingRule))), new ExecutorEngine(shardingProperties));
        } catch (final SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    private String getDatabaseProductName(final ShardingRule shardingRule) throws SQLException {
        String result = null;
        for (DataSource each : shardingRule.getDataSourceRule().getDataSources()) {
            String databaseProductName;
            if (each instanceof MasterSlaveDataSource) {
                databaseProductName = ((MasterSlaveDataSource) each).getDatabaseProductName();
            } else {
                try (Connection connection = each.getConnection()) {
                    databaseProductName = connection.getMetaData().getDatabaseProductName();
                }
            }
            Preconditions.checkState(null == result || result.equals(databaseProductName), String.format("Database type inconsistent with '%s' and '%s'", result, databaseProductName));
            result = databaseProductName;
        }
        return result;
    }
    
    @Override
    public ShardingConnection getConnection() throws SQLException {
        MetricsContext.init(shardingProperties);
        return new ShardingConnection(shardingContext);
    }
}
