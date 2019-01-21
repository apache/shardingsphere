/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxy.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaData;
import org.apache.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaData;

/**
 * Database type utility.
 * 
 * @author liuwei
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeUtil {
    
    private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    
    private static final String MYSQL8_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    
    private static final String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    
    private static final String ORACLE_DRIVER_CLASS_NAME = "oracle.jdbc.driver.OracleDriver";
    
    private static final String SQLSERVER_DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    private static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
    
    /**
     * Get database type.
     *
     * @param url url
     * @return database type
     */
    public static DatabaseType getDatabaseType(final String url) {
        try {
            new MySQLDataSourceMetaData(url);
            return DatabaseType.MySQL;
        } catch (final ShardingException ignore) {
        }
        try {
            new PostgreSQLDataSourceMetaData(url);
            return DatabaseType.PostgreSQL;
        } catch (final ShardingException ignore) {
        }
        try {
            new OracleDataSourceMetaData(url);
            return DatabaseType.Oracle;
        } catch (final ShardingException ignore) {
        }
        try {
            new SQLServerDataSourceMetaData(url);
            return DatabaseType.SQLServer;
        } catch (final ShardingException ignore) {
        }
        try {
            new H2DataSourceMetaData(url);
            return DatabaseType.H2;
        } catch (final ShardingException ignore) {
        }
        throw new ShardingException("Cannot resolve JDBC url `%s`.", url);
    }
    
    /**
     * Get driver class name.
     *
     * @param url url
     * @return driver class name
     */
    public static String getDriverClassName(final String url) {
        DatabaseType databaseType = getDatabaseType(url);
        switch (databaseType) {
            case MySQL:
                return getMySQLDriverClassName();
            case PostgreSQL:
                return POSTGRESQL_DRIVER_CLASS_NAME;
            case Oracle:
                return ORACLE_DRIVER_CLASS_NAME;
            case SQLServer:
                return SQLSERVER_DRIVER_CLASS_NAME;
            case H2:
                return H2_DRIVER_CLASS_NAME;
            default:
                throw new ShardingException("Cannot resolve JDBC url `%s`.", url);
        }
    }
    
    private static String getMySQLDriverClassName() {
        try {
            Class.forName(MYSQL8_DRIVER_CLASS_NAME);
            return MYSQL8_DRIVER_CLASS_NAME;
        } catch (final ClassNotFoundException ignore) {
            return MYSQL_DRIVER_CLASS_NAME;
        }
    }
}
