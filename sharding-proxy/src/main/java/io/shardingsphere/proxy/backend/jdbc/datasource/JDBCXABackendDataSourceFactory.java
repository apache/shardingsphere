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

package io.shardingsphere.proxy.backend.jdbc.datasource;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.manager.ShardingTransactionManagerRegistry;
import io.shardingsphere.transaction.manager.xa.XATransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;

/**
 * Backend data source factory using {@code AtomikosDataSourceBean} for JDBC and XA protocol.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class JDBCXABackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    private static final String XA_DRIVER_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
    
    @Override
    public DataSource build(final String dataSourceName, final DataSourceParameter dataSourceParameter) throws Exception {
        XATransactionManager xaTransactionManager = (XATransactionManager) ShardingTransactionManagerRegistry.getInstance().getShardingTransactionManager(TransactionType.XA);
        Class<XADataSource> xaDataSourceClass = loadClass(XA_DRIVER_CLASS_NAME);
        return xaTransactionManager.wrapDataSource(xaDataSourceClass.newInstance(), dataSourceName, dataSourceParameter);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> loadClass(final String className) throws ClassNotFoundException {
        Class result;
        try {
            result = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (final ClassNotFoundException ignored) {
            result = Class.forName(className);
        }
        return result;
    }
}
