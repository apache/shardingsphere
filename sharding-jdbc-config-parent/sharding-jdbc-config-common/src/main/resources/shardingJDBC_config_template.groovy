/**
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

import com.dangdang.ddframe.rdb.sharding.config.common.api.config.BindingTableRuleConfig
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.StrategyConfig
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.TableRuleConfig

/**
 * 名称转换.
 *
 * @param nameList
 * @return
 */
def convertName(def nameList) {
    nameList.split(',').collect({ "\"${it.trim()}\"" }).join(',')
}

/**
 * 策略生成闭包.
 */
def strategy(final String strategyName, final StrategyConfig strategyConfig) {
    if (strategyConfig) {
        def shardingColumns = convertName(strategyConfig.shardingColumns)
        if (strategyConfig.algorithmClassName) {
            return """new com.dangdang.ddframe.rdb.sharding.api.strategy.${
                strategyName == "databaseStrategy" ? 'database.DatabaseShardingStrategy' : 'table.TableShardingStrategy'
            }(${
                shardingColumns.contains(",") ? "[$shardingColumns]" : shardingColumns
            }, new $strategyConfig.algorithmClassName())"""
        } else if (strategyConfig.algorithmExpression) {
            return "$strategyName([$shardingColumns], {\"$strategyConfig.algorithmExpression\"})"
        } else {
            throw new NullPointerException("$strategyName configure at least one in 'algorithmClassName','algorithmExpression'")
        }
    }
    return ''
}

ShardingRuleConfig config = shardingRuleConfig

def result = new StringBuilder()

/*
 ShardingRuleConfig对象转换为groovy脚本.
 */
config.tables.each { String logicTable, TableRuleConfig tableRuleConfig ->
    result << """
table '$logicTable', [${convertName(tableRuleConfig.actualTables)}]${
        tableRuleConfig.databaseStrategy ? ", ${strategy('databaseStrategy', tableRuleConfig.databaseStrategy)}" : ''
    }${tableRuleConfig.tableStrategy ? ", ${strategy('tableStrategy', tableRuleConfig.tableStrategy)}" : ''}
"""
}

if (config.bindingTables && config.bindingTables.size() > 0) {
    config.bindingTables.each { BindingTableRuleConfig bindingTableRuleConfig ->
        result << "bind([${convertName(bindingTableRuleConfig.tableNames)}])\n"
    }
}

if (config.defaultDatabaseStrategy) {
    result << "defaultStrategy ${strategy('databaseStrategy', config.defaultDatabaseStrategy)}\n"
}

if (config.defaultTableStrategy) {
    result << "defaultStrategy ${strategy('tableStrategy', config.defaultTableStrategy)}\n"
}

return result
