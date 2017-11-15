package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.DataSourceService;

/**
 * @author junxiong
 */
public class EtcdDataSourceService implements DataSourceService {
    private CoordinatorRegistryCenter registryCenter;

    public EtcdDataSourceService(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    @Override
    public void persistDataSourcesNodeOnline(MasterSlaveDataSource masterSlaveDataSource) {

    }
}
