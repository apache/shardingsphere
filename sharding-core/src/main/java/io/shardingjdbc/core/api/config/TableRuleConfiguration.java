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

package io.shardingjdbc.core.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.keygen.KeyGenerator;
import io.shardingjdbc.core.keygen.KeyGeneratorFactory;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.core.util.InlineExpressionParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

/**
 * Table rule configuration.
 * 
 * @author zhangiang
 */
@Getter
@Setter
public class TableRuleConfiguration {
    
    private String logicTable;
    
    private String actualDataNodes;
    
    private ShardingStrategyConfiguration databaseShardingStrategyConfig;
    
    private ShardingStrategyConfiguration tableShardingStrategyConfig;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClass;
    
    private String logicIndex;
    
    /**
     * Build table rule.
     *
     * @param dataSourceNames data source names
     * @return table rule
     */
    public TableRule build(final Collection<String> dataSourceNames) {
        Preconditions.checkNotNull(logicTable, "Logic table cannot be null.");
        List<String> actualDataNodes = new InlineExpressionParser(this.actualDataNodes).evaluate();
        ShardingStrategy databaseShardingStrategy = null == databaseShardingStrategyConfig ? null : databaseShardingStrategyConfig.build();
        ShardingStrategy tableShardingStrategy = null == tableShardingStrategyConfig ? null : tableShardingStrategyConfig.build();
        KeyGenerator keyGenerator = !Strings.isNullOrEmpty(keyGeneratorColumnName) && !Strings.isNullOrEmpty(keyGeneratorClass) ? KeyGeneratorFactory.newInstance(keyGeneratorClass) : null;
        return new TableRule(logicTable, actualDataNodes, dataSourceNames, databaseShardingStrategy, tableShardingStrategy, keyGeneratorColumnName, keyGenerator, logicIndex);
    }
}
