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

import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.DatabaseType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * XA Data source registry.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XADataSourceRegistry {
    
    private static final Map<DatabaseType, String> XA_DATA_SOURCE_NAMES = new HashMap<>(DatabaseType.values().length, 1);
    
    static {
        XA_DATA_SOURCE_NAMES.put(DatabaseType.H2, "org.h2.jdbcx.JdbcDataSource");
        XA_DATA_SOURCE_NAMES.put(DatabaseType.MySQL, "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        XA_DATA_SOURCE_NAMES.put(DatabaseType.PostgreSQL, "org.postgresql.xa.PGXADataSource");
        XA_DATA_SOURCE_NAMES.put(DatabaseType.Oracle, "oracle.jdbc.xa.client.OracleXADataSource");
        XA_DATA_SOURCE_NAMES.put(DatabaseType.SQLServer, "com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
    }
    
    /**
     * Get XA data source class name.
     * 
     * @param databaseType database type
     * @return XA data source class name
     */
    public static String getXADataSourceClassName(final DatabaseType databaseType) {
        Preconditions.checkState(XA_DATA_SOURCE_NAMES.containsKey(databaseType), "Cannot support database type: `%s`", databaseType);
        return XA_DATA_SOURCE_NAMES.get(databaseType);
    }
}
