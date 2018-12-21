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

package io.shardingsphere.transaction.xa.convert.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.XADataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * XA data source factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
