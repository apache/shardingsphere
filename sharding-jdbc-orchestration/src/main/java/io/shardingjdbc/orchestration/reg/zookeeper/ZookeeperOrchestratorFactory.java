package io.shardingjdbc.orchestration.reg.zookeeper;

import io.shardingjdbc.orchestration.api.OrchestratorFactory;
import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestratorImpl;
import io.shardingjdbc.orchestration.reg.base.ConfigurationService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.DataSourceService;
import io.shardingjdbc.orchestration.reg.base.InstanceStateService;
import io.shardingjdbc.orchestration.reg.zookeeper.config.ZkConfigurationService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.datasource.ZkDataSourceService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.instance.ZkInstanceStateService;

/**
 * @author junxiong
 */
public class ZookeeperOrchestratorFactory implements OrchestratorFactory {

    @Override
    public boolean target(String type) {
        return "zk".equalsIgnoreCase(type);
    }

    /**
     * build specific orchestrator instance
     *
     * @return orchestrator instance
     */
    @Override
    public Orchestrator create(OrchestratorConfiguration configuration) {
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
