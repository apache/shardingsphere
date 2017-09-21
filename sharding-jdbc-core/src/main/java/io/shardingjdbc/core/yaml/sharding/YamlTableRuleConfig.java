package io.shardingjdbc.core.yaml.sharding;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
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
    
    private String actualTables;
    
    private String dataSourceNames;
    
    private YamlShardingStrategyConfig databaseStrategy;
    
    private YamlShardingStrategyConfig tableStrategy;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClass;
    
    /**
     * Build table rule.
     *
     * @return table rule
     */
    public TableRuleConfiguration getTableRuleConfig() {
        Preconditions.checkNotNull(logicTable, "Logic table cannot be null.");
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable(logicTable);
        tableRuleConfig.setActualTables(actualTables);
        tableRuleConfig.setLogicTable(logicTable);
        if (null != databaseStrategy) {
            tableRuleConfig.setDatabaseShardingStrategyConfig(databaseStrategy.getShardingStrategy());
        }
        if (null != tableStrategy) {
            tableRuleConfig.setTableShardingStrategyConfig(tableStrategy.getShardingStrategy());
        }
        tableRuleConfig.setKeyGeneratorClass(keyGeneratorClass);
        tableRuleConfig.setKeyGeneratorColumnName(keyGeneratorColumnName);
        return tableRuleConfig;
    }
}
