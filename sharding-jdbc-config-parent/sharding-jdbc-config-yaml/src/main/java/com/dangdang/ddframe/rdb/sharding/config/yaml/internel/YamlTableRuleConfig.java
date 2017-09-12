package com.dangdang.ddframe.rdb.sharding.config.yaml.internel;

import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

/**
 * Yaml table rule configuration.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlTableRuleConfig {
    
    private String logicTable;
    
    private boolean dynamic;
    
    private String actualTables;
    
    private String dataSourceNames;
    
    private YamlShardingStrategyConfig databaseShardingStrategy;
    
    private YamlShardingStrategyConfig tableShardingStrategy;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClass;
    
    /**
     * Build table rule.
     *
     * @return table rule
     */
    public TableRuleConfig getTableRuleConfig() {
        Preconditions.checkNotNull(logicTable, "Logic table cannot be null.");
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable(logicTable);
        tableRuleConfig.setDynamic(dynamic);
        tableRuleConfig.setActualTables(actualTables);
        tableRuleConfig.setLogicTable(logicTable);
        if (null != databaseShardingStrategy) {
            tableRuleConfig.setDatabaseShardingStrategyConfig(databaseShardingStrategy.getShardingStrategy());
        }
        if (null != tableShardingStrategy) {
            tableRuleConfig.setTableShardingStrategyConfig(tableShardingStrategy.getShardingStrategy());
        }
        tableRuleConfig.setKeyGeneratorClass(keyGeneratorClass);
        tableRuleConfig.setKeyGeneratorColumnName(keyGeneratorColumnName);
        return tableRuleConfig;
    }
}
