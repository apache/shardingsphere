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

package io.shardingjdbc.core.yaml.sharding;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.keygen.KeyGeneratorFactory;
import lombok.Getter;
import lombok.Setter;

/**
 * Yaml table rule configuration.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlTableRuleConfiguration {
    
    private String logicTable;
    
    private String actualDataNodes;
    
    private YamlShardingStrategyConfiguration databaseStrategy;
    
    private YamlShardingStrategyConfiguration tableStrategy;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClass;
    
    private String logicIndex;
    
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
        if (!Strings.isNullOrEmpty(keyGeneratorClass)) {
            result.setKeyGenerator(KeyGeneratorFactory.newInstance(keyGeneratorClass));
        }
        result.setKeyGeneratorColumnName(keyGeneratorColumnName);
        result.setLogicIndex(logicIndex);
        return result;
    }
}
