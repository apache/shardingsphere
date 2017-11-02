package io.shardingjdbc.orchestration.spring.boot.masterslave;

import io.shardingjdbc.orchestration.yaml.YamlOrchestrationMasterSlaveRuleConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Orchestration master-slave rule configuration properties.
 *
 * @author caohao
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config.masterslave")
public class OrchestrationSpringBootMasterSlaveRuleConfigurationProperties extends YamlOrchestrationMasterSlaveRuleConfiguration {
}
