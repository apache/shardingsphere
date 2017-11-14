package io.shardingjdbc.orchestration.reg.base;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;

/**
 * DataSource Service
 *
 * @author junxiong
 */
public interface DataSourceService {
    /**
     * Persist master-salve datasources node and add listener.
     *
     * @param masterSlaveDataSource master-slave datasource
     */
    void persistDataSourcesNodeOnline(MasterSlaveDataSource masterSlaveDataSource);
}
