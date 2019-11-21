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

package io.shardingsphere.core.yaml.sharding;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.keygen.KeyGeneratorFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Yaml table rule configuration.
 *
 * @author caohao
 * @author panjuan
 */
@NoArgsConstructor
@Getter
@Setter
public class YamlTableRuleConfiguration {
    
    private String logicTable;
    
    private String actualDataNodes;
    
    private YamlShardingStrategyConfiguration databaseStrategy;
    
    private YamlShardingStrategyConfiguration tableStrategy;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClassName;
    
    private String logicIndex;
    
    public YamlTableRuleConfiguration(final TableRuleConfiguration tableRuleConfiguration) {
        logicTable = tableRuleConfiguration.getLogicTable();
        actualDataNodes = tableRuleConfiguration.getActualDataNodes();
        databaseStrategy = new YamlShardingStrategyConfiguration(tableRuleConfiguration.getDatabaseShardingStrategyConfig());
        tableStrategy = new YamlShardingStrategyConfiguration(tableRuleConfiguration.getTableShardingStrategyConfig());
        keyGeneratorColumnName = tableRuleConfiguration.getKeyGeneratorColumnName();
        keyGeneratorClassName = null == tableRuleConfiguration.getKeyGenerator()
                ? null : tableRuleConfiguration.getKeyGenerator().getClass().getName();
    }
    
    /**
     * Build table rule configuration.
     *
     * @return table rule configuration
     */
    public TableRuleConfiguration build() {
        Preconditions.checkNotNull(logicTable, "Logic table cannot be null.");
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable(logicTable);
        result.setActualDataNodes(actualDataNodes);
        if (null != databaseStrategy) {
            result.setDatabaseShardingStrategyConfig(databaseStrategy.build());
        }
        if (null != tableStrategy) {
            result.setTableShardingStrategyConfig(tableStrategy.build());
        }
        if (!Strings.isNullOrEmpty(keyGeneratorClassName)) {
            result.setKeyGenerator(KeyGeneratorFactory.newInstance(keyGeneratorClassName));
        }
        result.setKeyGeneratorColumnName(keyGeneratorColumnName);
        result.setLogicIndex(logicIndex);
        return result;
    }
}
