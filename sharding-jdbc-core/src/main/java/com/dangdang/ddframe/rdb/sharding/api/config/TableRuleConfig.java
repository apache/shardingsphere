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

package com.dangdang.ddframe.rdb.sharding.api.config;

import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGeneratorFactory;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.util.InlineExpressionParser;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Table rule configuration.
 * 
 * @author zhangiang
 */
@Getter
@Setter
public class TableRuleConfig {
    
    private String logicTable;
    
    private boolean dynamic;
    
    private String actualTables;
    
    private String dataSourceNames;
    
    private ShardingStrategyConfig databaseShardingStrategyConfig;
    
    private ShardingStrategyConfig tableShardingStrategyConfig;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClass;
    
    /**
     * Build table rule.
     *
     * @param dataSourceMap data source map
     * @return table rule
     */
    public TableRule build(final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkNotNull(logicTable, "Logic table cannot be null.");
        List<String> actualTables = new InlineExpressionParser(this.actualTables).evaluate();
        List<String> dataSourceNames = new InlineExpressionParser(this.dataSourceNames).evaluate();
        ShardingStrategy databaseShardingStrategy = null == databaseShardingStrategyConfig ? null : databaseShardingStrategyConfig.build();
        ShardingStrategy tableShardingStrategy = null == tableShardingStrategyConfig ? null : tableShardingStrategyConfig.build();
        KeyGenerator keyGenerator = null != keyGeneratorColumnName && null != keyGeneratorClass ? KeyGeneratorFactory.newInstance(keyGeneratorClass) : null;
        return new TableRule(logicTable, dynamic, actualTables, dataSourceNames, dataSourceMap, databaseShardingStrategy, tableShardingStrategy, keyGeneratorColumnName, keyGenerator);
    }
}
