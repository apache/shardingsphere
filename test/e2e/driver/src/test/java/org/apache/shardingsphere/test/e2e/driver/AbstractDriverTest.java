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

package org.apache.shardingsphere.test.e2e.driver;

import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO merge in to sql-e2e
public abstract class AbstractDriverTest {
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Arrays.asList("jdbc_0", "jdbc_1", "single_jdbc", "shadow_jdbc_0", "shadow_jdbc_1", "encrypt", "test_primary_ds", "test_replica_ds");
    
    private static final Map<String, DataSource> ACTUAL_DATA_SOURCES = new HashMap<>(ACTUAL_DATA_SOURCE_NAMES.size(), 1F);
    
    @BeforeAll
    static synchronized void initializeDataSource() throws SQLException {
        for (String each : ACTUAL_DATA_SOURCE_NAMES) {
            ACTUAL_DATA_SOURCES.put(each, buildDataSource(each));
            initializeSchema(each);
        }
    }
    
    private static void initializeSchema(final String dataSourceName) throws SQLException {
        try (Connection connection = ACTUAL_DATA_SOURCES.get(dataSourceName).getConnection()) {
            if ("encrypt".equals(dataSourceName)) {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/jdbc_encrypt_init.sql"))));
            } else if ("shadow_jdbc_0".equals(dataSourceName) || "shadow_jdbc_1".equals(dataSourceName)) {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/jdbc_shadow_init.sql"))));
            } else if ("single_jdbc".equals(dataSourceName)) {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/single_jdbc_init.sql"))));
            } else {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/jdbc_init.sql"))));
            }
        }
    }
    
    private static DataSource buildDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName));
        result.setUsername("sa");
        result.setPassword("");
        result.setMinimumIdle(0);
        result.setMaximumPoolSize(50);
        return result;
    }
    
    protected static Map<String, DataSource> getActualDataSources() {
        return ACTUAL_DATA_SOURCES;
    }
}
