/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.integrate.type.sharding;

import com.dangdang.ddframe.rdb.common.base.AbstractSQLAssertTest;
import com.dangdang.ddframe.rdb.common.env.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.integrate.fixture.ComplexKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.integrate.jaxb.helper.SQLAssertJAXBHelper;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.AfterClass;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NullableShardingTableOnlyTest extends AbstractSQLAssertTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public NullableShardingTableOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> dataParameters() {
        return SQLAssertJAXBHelper.getDataParameters("integrate/assert/select_aggregate.xml");
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.nullable;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/nullable/init/nullable_0.xml",
                "integrate/dataset/sharding/nullable/init/nullable_1.xml",
                "integrate/dataset/sharding/nullable/init/nullable_2.xml",
                "integrate/dataset/sharding/nullable/init/nullable_3.xml",
                "integrate/dataset/sharding/nullable/init/nullable_4.xml",
                "integrate/dataset/sharding/nullable/init/nullable_5.xml",
                "integrate/dataset/sharding/nullable/init/nullable_6.xml",
                "integrate/dataset/sharding/nullable/init/nullable_7.xml",
                "integrate/dataset/sharding/nullable/init/nullable_8.xml",
                "integrate/dataset/sharding/nullable/init/nullable_9.xml");
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
            shardingRuleConfig.setDataSources(each.getValue());
            TableRuleConfig tableRuleConfig = new TableRuleConfig();
            tableRuleConfig.setLogicTable("t_order");
            shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
            ComplexShardingStrategyConfig databaseShardingStrategyConfig = new ComplexShardingStrategyConfig();
            databaseShardingStrategyConfig.setShardingColumns("user_id");
            databaseShardingStrategyConfig.setAlgorithmClassName(ComplexKeysModuloDatabaseShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
            ShardingRule shardingRule = shardingRuleConfig.build();
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
        return shardingDataSources;
    }
    
    @AfterClass
    public static void clear() {
        isShutdown = true;
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
            }
        }
    }
}
