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
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable(logicTable);
        tableRuleConfig.setActualDataNodes(actualDataNodes);
        tableRuleConfig.setLogicTable(logicTable);
        if (null != databaseStrategy) {
            tableRuleConfig.setDatabaseShardingStrategyConfig(databaseStrategy.build());
        }
        if (null != tableStrategy) {
            tableRuleConfig.setTableShardingStrategyConfig(tableStrategy.build());
        }
        tableRuleConfig.setKeyGeneratorClass(keyGeneratorClass);
        tableRuleConfig.setKeyGeneratorColumnName(keyGeneratorColumnName);
        tableRuleConfig.setLogicIndex(logicIndex);
        return tableRuleConfig;
    }
}
