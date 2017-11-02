package io.shardingjdbc.orchestration.spring.boot.orchestration;

import io.shardingjdbc.orchestration.yaml.YamlOrchestrationConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Orchestration configuration properties.
 *
 * @author caohao
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config.orchestration")
public class OrchestrationSpringBootConfigurationProperties extends YamlOrchestrationConfiguration {
}
