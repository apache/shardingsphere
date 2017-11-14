package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestratorImpl;
import io.shardingjdbc.orchestration.reg.base.*;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperRegistryCenter;
import io.shardingjdbc.orchestration.reg.zookeeper.config.ZkConfigurationService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.datasource.ZkDataSourceService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.instance.ZkInstanceStateService;

/**
 * Orchestrator builder
 *
 * @author junxiong
 */
public class OrchestratorBuilder {

    private OrchestratorConfiguration configuration;

    /**
     * New orchestrator builder
     * @return OrchestratorBuilder
     */
    public static OrchestratorBuilder newBuilder() {
        return new OrchestratorBuilder();
    }

    /**
     * with orchestorator configuration
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
        // TODO build different kind of orchestrator
        String name = configuration.getName();
        boolean overwrite = configuration.isOverwrite();
        CoordinatorRegistryCenter registryCenter = setupRegistryCenterIfNeeded(configuration);
        ConfigurationService configurationService = new ZkConfigurationService(name, overwrite, registryCenter);
        InstanceStateService instanceStateService = new ZkInstanceStateService(name, configurationService, registryCenter);
        DataSourceService dataSourceService = new ZkDataSourceService(name, configurationService, registryCenter);
        return new OrchestratorImpl(configurationService, instanceStateService, dataSourceService);
    }

    private CoordinatorRegistryCenter setupRegistryCenterIfNeeded(OrchestratorConfiguration config) {
        ZookeeperConfiguration zkConfig = ZookeeperConfiguration.from(config);
        CoordinatorRegistryCenter registryCenter =  new ZookeeperRegistryCenter(zkConfig);
        registryCenter.init();
        return registryCenter;
    }
}
