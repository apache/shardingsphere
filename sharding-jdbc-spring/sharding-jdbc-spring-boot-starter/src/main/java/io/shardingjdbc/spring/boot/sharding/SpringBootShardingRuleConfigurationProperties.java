package io.shardingjdbc.spring.boot.sharding;

import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sharding rule configuration properties.
 *
 * @author caohao
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config.sharding")
public class SpringBootShardingRuleConfigurationProperties extends YamlShardingConfiguration {
}
