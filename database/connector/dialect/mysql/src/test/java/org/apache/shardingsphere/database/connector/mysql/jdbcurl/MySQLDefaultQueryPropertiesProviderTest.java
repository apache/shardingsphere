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

package org.apache.shardingsphere.database.connector.mysql.jdbcurl;

import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectDefaultQueryPropertiesProvider;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLDefaultQueryPropertiesProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectDefaultQueryPropertiesProvider provider = DatabaseTypedSPILoader.getService(DialectDefaultQueryPropertiesProvider.class, databaseType);
    
    @Test
    void assertGetDefaultQueryProperties() {
        Properties actual = provider.getDefaultQueryProperties();
        Properties expected = PropertiesBuilder.build(
                new Property("useServerPrepStmts", Boolean.TRUE.toString()),
                new Property("cachePrepStmts", Boolean.TRUE.toString()),
                new Property("prepStmtCacheSize", "8192"),
                new Property("prepStmtCacheSqlLimit", "2048"),
                new Property("useLocalSessionState", Boolean.TRUE.toString()),
                new Property("rewriteBatchedStatements", Boolean.TRUE.toString()),
                new Property("cacheResultSetMetadata", Boolean.FALSE.toString()),
                new Property("cacheServerConfiguration", Boolean.TRUE.toString()),
                new Property("elideSetAutoCommits", Boolean.TRUE.toString()),
                new Property("maintainTimeStats", Boolean.FALSE.toString()),
                new Property("netTimeoutForStreamingResults", "0"),
                new Property("tinyInt1isBit", Boolean.FALSE.toString()),
                new Property("useSSL", Boolean.FALSE.toString()),
                new Property("zeroDateTimeBehavior", "round"));
        assertThat(actual, is(expected));
    }
}
