package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.reg.base.DataSourceService;

/**
 * @author junxiong
 */
public class EtcdDataSourceService implements DataSourceService {
    @Override
    public void persistDataSourcesNodeOnline(MasterSlaveDataSource masterSlaveDataSource) {

    }
}
