package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Optional;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.orchestration.reg.base.*;
import io.shardingjdbc.orchestration.reg.etcd.internal.RegistryPath;

/**
 * @author junxiong
 */
public class EtcdDataSourceServiceImpl implements DataSourceService {
    private CoordinatorRegistryCenter registryCenter;
    private ConfigurationService configurationService;
    private RegistryPath dataSourceNodePath;

    EtcdDataSourceServiceImpl(String name, ConfigurationService configurationService, CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        this.configurationService = configurationService;
        this.dataSourceNodePath = RegistryPath.from(name, "state", "datasources");
    }

    @Override
    public void persistDataSourcesNodeOnline(final MasterSlaveDataSource masterSlaveDataSource) {
        registryCenter.persist(dataSourceNodePath.asNodePath(), StateNodeStatus.ENABLED.name());
        addDataSourcesNodeListener(masterSlaveDataSource);
    }

    private void addDataSourcesNodeListener(final MasterSlaveDataSource masterSlaveDataSource) {
        registryCenter.addRegistryChangeListener(dataSourceNodePath.asNodePath(), new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                Optional<RegistryChangeEvent.Payload> payload = registryChangeEvent.getPayload();
                if (payload.isPresent()) {
                    MasterSlaveRule masterSlaveRule = configurationService.getAvailableMasterSlaveRule();
                    if (RegistryChangeType.UPDATED == registryChangeEvent.getType()) {
                        String datasourceKey = payload.get().getKey();
                        String dataSourceName = datasourceKey.substring(datasourceKey.lastIndexOf("/") + 1);
                        masterSlaveRule.getSlaveDataSourceMap().remove(dataSourceName);
                    }
                    masterSlaveDataSource.renew(masterSlaveRule);
                }
            }
        });
    }
}
