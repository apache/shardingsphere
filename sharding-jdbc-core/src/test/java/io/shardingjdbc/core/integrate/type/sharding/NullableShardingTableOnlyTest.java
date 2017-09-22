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

package io.shardingjdbc.core.integrate.type.sharding;

import io.shardingjdbc.core.common.base.AbstractSQLAssertTest;
import io.shardingjdbc.core.common.env.ShardingTestStrategy;
import io.shardingjdbc.core.integrate.fixture.ComplexKeysModuloDatabaseShardingAlgorithm;
import io.shardingjdbc.core.integrate.jaxb.SQLShardingRule;
import io.shardingjdbc.core.integrate.jaxb.helper.SQLAssertJAXBHelper;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.junit.AfterClass;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
        List<Object[]> result = new LinkedList<>();
        result.addAll(SQLAssertJAXBHelper.getDataParameters("integrate/assert/select_aggregate.xml"));
        result.addAll(SQLAssertJAXBHelper.getDataParameters("integrate/assert/select_nullable.xml"));
        return result;
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
            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
            tableRuleConfig.setLogicTable("t_order");
            shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id", ComplexKeysModuloDatabaseShardingAlgorithm.class.getName()));
            ShardingRule shardingRule = shardingRuleConfig.build(each.getValue());
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
