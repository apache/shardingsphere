package io.shardingjdbc.spring.boot;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sharding jdbc spring boot properties.
 *
 * @author caohao
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config")
@Getter
@Setter
public class ShardingProperties {
    
    private ShardingRuleConfiguration sharding;
    
    private MasterSlaveRuleConfiguration masterslave;
}
