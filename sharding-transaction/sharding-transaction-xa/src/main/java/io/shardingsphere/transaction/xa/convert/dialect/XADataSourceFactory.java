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

package io.shardingsphere.transaction.xa.convert.dialect;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.XADataSource;

/**
 * Create XADatasource based database type.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XADataSourceFactory {
    
    /**
     * Create XA DataSource instance.
     *
     * @param databaseType database type
     * @return XA DataSource instance
     */
    public static XADataSource build(final DatabaseType databaseType) {
        switch (databaseType) {
            case MySQL:
                return newInstance(XADatabaseType.MySQL.getClassName());
            case H2:
                return newInstance(XADatabaseType.H2.getClassName());
            case Oracle:
                return newInstance(XADatabaseType.Oracle.getClassName());
            case SQLServer:
                return newInstance(XADatabaseType.SQLServer.getClassName());
            case PostgreSQL:
                return newInstance(XADatabaseType.PostgreSQL.getClassName());
            default:
                throw new UnsupportedOperationException(String.format("Database [%s] Cannot support XA.", databaseType));
        }
    }
    
    @SuppressWarnings("unchecked")
    private static XADataSource newInstance(final String className) {
        Class xaDataSourceClass;
        try {
            xaDataSourceClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (final ClassNotFoundException ignored) {
            try {
                xaDataSourceClass = Class.forName(className);
            } catch (final ClassNotFoundException ex) {
                throw new ShardingException(String.format("Failed to load [%s]", className));
            }
        }
        try {
            return (XADataSource) xaDataSourceClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ShardingException(String.format("Failed to instance [%s]", className));
        }
    }
}
