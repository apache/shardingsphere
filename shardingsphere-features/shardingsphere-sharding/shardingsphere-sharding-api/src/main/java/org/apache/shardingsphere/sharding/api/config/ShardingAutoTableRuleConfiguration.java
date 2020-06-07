package org.apache.shardingsphere.sharding.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.config.strategy.ShardingStrategyConfiguration;

/**
 * Sharding automatic table rule configuration.
 *
 */
@Getter
@Setter
public class ShardingAutoTableRuleConfiguration {

    private final String logicTable;

    private final String actualDataSources;

    private ShardingStrategyConfiguration shardingStrategy;

    private KeyGeneratorConfiguration keyGenerator;

    public ShardingAutoTableRuleConfiguration(final String logicTable) {
        this(logicTable, null);
    }

    public ShardingAutoTableRuleConfiguration(final String logicTable, final String actualDataSources) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(logicTable), "LogicTable is required.");
        this.logicTable = logicTable;
        this.actualDataSources = actualDataSources;
    }
}
