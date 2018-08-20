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

package io.shardingsphere.opentracing.fixture;

import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingContextBuilder {
    
    /**
     * Build sharding context.
     * 
     * @return sharding context
     * @throws SQLException SQL exception
     */
    public static ShardingContext build() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", mockDataSource());
        dataSourceMap.put("ds_1", mockDataSource());
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceMap.keySet());
        ShardingContext shardingContext = Mockito.mock(ShardingContext.class);
        when(shardingContext.getShardingRule()).thenReturn(shardingRule);
        ShardingMetaData shardingMetaData = Mockito.mock(ShardingMetaData.class);
        Map<String, String> dataSourceUrls = new LinkedHashMap<>();
        dataSourceUrls.put("ds_0", "jdbc:mysql://127.0.0.1:3306/ds");
        when(shardingMetaData.getDataSource()).thenReturn(new ShardingDataSourceMetaData(dataSourceUrls, shardingRule, DatabaseType.MySQL));
        when(shardingMetaData.getTable()).thenReturn(new ShardingTableMetaData(new LinkedHashMap<String, TableMetaData>()));
        when(shardingContext.getMetaData()).thenReturn(shardingMetaData);
        when(shardingContext.getDatabaseType()).thenReturn(DatabaseType.MySQL);
        when(shardingContext.isShowSQL()).thenReturn(true);
        return shardingContext;
    }
    
    private static DataSource mockDataSource() throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/ds");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
}
