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

package io.shardingsphere.core.parsing.parser.sql;

import com.google.common.collect.Lists;
import io.shardingsphere.core.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingsphere.core.metadata.ColumnMetaData;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.TableMetaData;
import io.shardingsphere.core.rule.ShardingRule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractStatementParserTest {
    
    protected final ShardingRule createShardingRule() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("TABLE_XXX");
        tableRuleConfig.setActualDataNodes("ds.table_${0..2}");
        tableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("field1, field2, field3, field4, field5, field6, field7", new TestComplexKeysShardingAlgorithm()));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds"));
    }
    
    protected final ShardingMetaData createShardingMetaData() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
        tableMetaDataMap.put("TABLE_XXX", getTableMetaData(new ArrayList<String>(){{add("field1");add("field2");}}));
        ShardingMetaData shardingMetaData = mock(ShardingMetaData.class);
        when(shardingMetaData.getTableMetaDataMap()).thenReturn(tableMetaDataMap);
        return shardingMetaData;
    }
    
    private static TableMetaData getTableMetaData(final List<String> columnNames) {
        List<ColumnMetaData> columnMetaDataList = new ArrayList<>();
        for (String columnName : columnNames) {
            columnMetaDataList.add(new ColumnMetaData(columnName, "int(11)", ""));
        }
        return new TableMetaData(columnMetaDataList);
    }
}
