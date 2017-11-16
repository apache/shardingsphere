package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.api.OrchestratorFactory;
import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestratorImpl;
import io.shardingjdbc.orchestration.reg.base.ConfigurationService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.DataSourceService;
import io.shardingjdbc.orchestration.reg.base.InstanceStateService;

/**
 * Etcd Orchestrator factory
 *
 * @author junxiong
 */
public class EtcdOrchestratorFactory implements OrchestratorFactory {

    @Override
    public boolean target(String type) {
        return "etcd".equalsIgnoreCase(type);
    }

    @Override
    public Orchestrator create(OrchestratorConfiguration configuration) {
        String name = configuration.getName();
        boolean overwrite = configuration.isOverwrite();
        CoordinatorRegistryCenter registryCenter = setUpCoordinatorRegistryCenter(configuration);
        ConfigurationService configurationService = new EtcdConfigurationServiceImpl(name, overwrite, registryCenter);
        DataSourceService dataSourceService = new EtcdDataSourceServiceImpl(name, configurationService, registryCenter);
        InstanceStateService instanceStateService = new EtcdInstanceStateServiceImpl(name, configurationService, registryCenter);
        return new OrchestratorImpl(configurationService, instanceStateService, dataSourceService);
    }

    private CoordinatorRegistryCenter setUpCoordinatorRegistryCenter(OrchestratorConfiguration configuration) {
        return new EtcdRegistryCenter(EtcdConfiguration.builder()
                .namespace(configuration.getRegistryCenter().get("namespace"))
                .serverLists(configuration.getRegistryCenter().get("server-lists"))
                .build());
    }
}
