/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.convert.datasource.XADataSourceFactory;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerSPILoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * Backend data source factory using {@code AtomikosDataSourceBean} for JDBC and XA protocol.
 *
 * @author zhaojun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCXABackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    private static final JDBCXABackendDataSourceFactory INSTANCE = new JDBCXABackendDataSourceFactory();
    
    /**
     * Get instance of {@code JDBCXABackendDataSourceFactory}.
     *
     * @return JDBC XA backend data source factory
     */
    public static JDBCBackendDataSourceFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public DataSource build(final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        XATransactionManager xaTransactionManager = XATransactionManagerSPILoader.getInstance().getTransactionManager();
        return xaTransactionManager.wrapDataSource(DatabaseType.MySQL, XADataSourceFactory.build(DatabaseType.MySQL), dataSourceName, dataSourceParameter);
    }
}
