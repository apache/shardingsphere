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
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.keygen.BuiltinKeyGeneratorType;
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
    
    private YamlKeyGeneratorConfiguration keyGenerator;
    
    private String logicIndex;
    
    public YamlTableRuleConfiguration(final TableRuleConfiguration tableRuleConfiguration) {
        logicTable = tableRuleConfiguration.getLogicTable();
        actualDataNodes = tableRuleConfiguration.getActualDataNodes();
        databaseStrategy = new YamlShardingStrategyConfiguration(tableRuleConfiguration.getDatabaseShardingStrategyConfig());
        tableStrategy = new YamlShardingStrategyConfiguration(tableRuleConfiguration.getTableShardingStrategyConfig());
        keyGenerator = null == tableRuleConfiguration.getKeyGenerator() ? null : getYamlKeyGeneratorConfiguration(tableRuleConfiguration);
    }
    
    private YamlKeyGeneratorConfiguration getYamlKeyGeneratorConfiguration(final TableRuleConfiguration tableRuleConfiguration) {
        YamlKeyGeneratorConfiguration result = new YamlKeyGeneratorConfiguration();
        String keyGeneratorClassName = tableRuleConfiguration.getKeyGenerator().getClass().getName();
        BuiltinKeyGeneratorType keyGeneratorType = BuiltinKeyGeneratorType.getBuiltinKeyGeneratorType(keyGeneratorClassName);
        if (null == keyGeneratorType) {
            result.setKeyGeneratorClassName(keyGeneratorClassName);
        } else {
            result.setKeyGeneratorType(keyGeneratorType.name());
        }
        result.setKeyGeneratorColumnName(tableRuleConfiguration.getKeyGeneratorColumnName());
        result.setProps(tableRuleConfiguration.getKeyGenerator().getProperties());
        return result;
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
        result.setKeyGenerator(keyGenerator.getKeyGenerator());
        result.setKeyGeneratorColumnName(keyGenerator.getKeyGeneratorColumnName());
        result.setLogicIndex(logicIndex);
        return result;
    }
}
