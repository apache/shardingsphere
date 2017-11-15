package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperOrchestratorFactory;

import java.util.ServiceLoader;

/**
 * Orchestrator builder
 *
 * @author junxiong
 */
public class OrchestratorBuilder {

    private OrchestratorConfiguration configuration;

    /**
     * New orchestrator builder
     *
     * @return OrchestratorBuilder
     */
    public static OrchestratorBuilder newBuilder() {
        return new OrchestratorBuilder();
    }

    /**
     * with orchestorator configuration
     *
     * @param configuration orchestrator configuration
     * @return self
     */
    public OrchestratorBuilder with(OrchestratorConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * build specific orchestrator instance
     *
     * @return orchestrator instance
     */
    public Orchestrator build() {
        String type = configuration.getRegistryCenter().get("type");
        ServiceLoader<OrchestratorFactory> serviceLoader = ServiceLoader.load(OrchestratorFactory.class);
        for (OrchestratorFactory orchestratorFactory : serviceLoader) {
            if (orchestratorFactory.target(type)) {
                return orchestratorFactory.create(configuration);
            }
        }
        return new ZookeeperOrchestratorFactory().create(configuration);
    }

}
