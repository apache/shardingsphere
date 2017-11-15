package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;

/**
 * Orchestrator factory
 *
 * @author junxiong
 */
public interface OrchestratorFactory {
    boolean target(String type);
    Orchestrator create(OrchestratorConfiguration configuration);
}
