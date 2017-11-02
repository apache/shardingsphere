package io.shardingjdbc.orchestration.spring.boot.sharding;

import io.shardingjdbc.orchestration.yaml.YamlOrchestrationShardingRuleConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Orchestration sharding rule configuration properties.
 *
 * @author caohao
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config.sharding")
public class OrchestrationSpringBootShardingRuleConfigurationProperties extends YamlOrchestrationShardingRuleConfiguration {
}
