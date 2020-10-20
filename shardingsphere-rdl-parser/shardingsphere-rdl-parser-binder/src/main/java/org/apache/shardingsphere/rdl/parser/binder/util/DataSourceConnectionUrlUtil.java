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

package org.apache.shardingsphere.rdl.parser.binder.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.*;
import org.apache.shardingsphere.rdl.parser.statement.rdl.DataSourceConnectionSegment;

/**
 * Data source connection url util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceConnectionUrlUtil {
    
    /**
     * Get url.
     *
     * @param connectionSegment connection segment
     * @param databaseType database type
     * @return url
     */
    public static String getUrl(final DataSourceConnectionSegment connectionSegment, final DatabaseType databaseType) {
        switch (databaseType.getName()) {
            case "MySQL":
                return getUrl(connectionSegment, new MySQLDatabaseType().getJdbcUrlPrefixes().iterator().next());
            case "PostgreSQL":
                return getUrl(connectionSegment, new PostgreSQLDatabaseType().getJdbcUrlPrefixes().iterator().next());
            case "MariaDB":
                return getUrl(connectionSegment, new MariaDBDatabaseType().getJdbcUrlPrefixes().iterator().next());
            case "Oracle":
                return getUrl(connectionSegment, new OracleDatabaseType().getJdbcUrlPrefixes().iterator().next());
            case "SQLServer":
                return getUrl(connectionSegment, new SQLServerDatabaseType().getJdbcUrlPrefixes().iterator().next());
            default:
                throw new UnsupportedOperationException(String.format("ShardingSphere can not get url from %s.", databaseType.getName()));
        }
    }
    
    private static String getUrl(final DataSourceConnectionSegment connectionSegment, final String jdbcUrlPrefix) {
        return String.format("%s//%s:%s/%s", jdbcUrlPrefix,
                connectionSegment.getHostName(), connectionSegment.getPort(), connectionSegment.getDb());
    }
}
