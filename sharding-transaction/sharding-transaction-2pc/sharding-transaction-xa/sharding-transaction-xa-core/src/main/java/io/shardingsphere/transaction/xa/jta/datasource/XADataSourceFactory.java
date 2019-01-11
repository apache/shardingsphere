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

package io.shardingsphere.transaction.xa.jta.datasource;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.convert.swap.DataSourceSwapperRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * XA data source factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class XADataSourceFactory {
    
    private static final Map<DatabaseType, String> XA_DRIVER_CLASS_NAMES = new HashMap<>(DatabaseType.values().length, 1);
    
    static {
        XA_DRIVER_CLASS_NAMES.put(DatabaseType.H2, "org.h2.jdbcx.JdbcDataSource");
        XA_DRIVER_CLASS_NAMES.put(DatabaseType.MySQL, "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        XA_DRIVER_CLASS_NAMES.put(DatabaseType.PostgreSQL, "org.postgresql.xa.PGXADataSource");
        XA_DRIVER_CLASS_NAMES.put(DatabaseType.Oracle, "oracle.jdbc.xa.client.OracleXADataSource");
        XA_DRIVER_CLASS_NAMES.put(DatabaseType.SQLServer, "com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
    }
    
    /**
     * Create XA DataSource instance.
     *
     * @param databaseType database type
     * @return XA DataSource instance
     */
    public static XADataSource build(final DatabaseType databaseType) {
        return newXADataSourceInstance(databaseType);
    }
    
    /**
     * Create XA data source through general data source.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @return XA data source
     */
    public static XADataSource build(final DatabaseType databaseType, final DataSource dataSource) {
        try {
            DataSourceParameter dataSourceParameter = DataSourceSwapperRegistry.getSwapper(dataSource.getClass()).swap(dataSource);
            XADataSource xaDataSource = newXADataSourceInstance(databaseType);
            Properties xaProperties = XAPropertiesFactory.createXAProperties(databaseType).build(dataSourceParameter);
            PropertyUtils.setProperties(xaDataSource, xaProperties);
            return xaDataSource;
        } catch (final PropertyException ex) {
            log.error("Failed to create ShardingXADataSource.");
            throw new ShardingException(ex);
        }
    }
    
    private static XADataSource newXADataSourceInstance(final DatabaseType databaseType) {
        String xaDataSourceClassName = XA_DRIVER_CLASS_NAMES.get(databaseType);
        Class xaDataSourceClass;
        try {
            xaDataSourceClass = Thread.currentThread().getContextClassLoader().loadClass(xaDataSourceClassName);
        } catch (final ClassNotFoundException ignored) {
            try {
                xaDataSourceClass = Class.forName(xaDataSourceClassName);
            } catch (final ClassNotFoundException ex) {
                throw new ShardingException("Failed to load [%s]", xaDataSourceClassName);
            }
        }
        try {
            return (XADataSource) xaDataSourceClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ShardingException("Failed to instance [%s]", xaDataSourceClassName);
        }
    }
}
