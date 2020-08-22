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

package org.apache.shardingsphere.driver.common.base;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.driver.common.env.DatabaseEnvironment;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractSQLTest {
    
    private static final List<String> DB_NAMES = Arrays.asList("jdbc_0", "jdbc_1", "encrypt", "test_ds_master", "test_ds_slave");
    
    private static final Set<DatabaseType> DATABASE_TYPES = Sets.newHashSet(DatabaseTypes.getActualDatabaseType("H2"));
    
    @Getter(AccessLevel.PROTECTED)
    private static final Map<DatabaseType, Map<String, DataSource>> DATABASE_TYPE_MAP = new HashMap<>();
    
    @BeforeClass
    public static synchronized void initDataSource() {
        createDataSources();
    }
    
    private static void createDataSources() {
        for (String each : DB_NAMES) {
            for (DatabaseType type : DATABASE_TYPES) {
                createDataSources(each, type);
            }
        }
    }
    
    private static void createDataSources(final String dbName, final DatabaseType databaseType) {
        DATABASE_TYPE_MAP.computeIfAbsent(databaseType, key -> new LinkedHashMap<>()).put(dbName, buildDataSource(dbName, databaseType));
        createSchema(dbName, databaseType);
    }
    
    private static BasicDataSource buildDataSource(final String dbName, final DatabaseType databaseType) {
        DatabaseEnvironment dbEnv = new DatabaseEnvironment(databaseType);
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(dbEnv.getDriverClassName());
        result.setUrl(dbEnv.getURL(dbName));
        result.setUsername(dbEnv.getUsername());
        result.setPassword(dbEnv.getPassword());
        result.setMaxTotal(50);
        return result;
    }
    
    private static void createSchema(final String dbName, final DatabaseType databaseType) {
        try {
            Connection conn = DATABASE_TYPE_MAP.get(databaseType).get(dbName).getConnection();
            RunScript.execute(conn, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("jdbc_init.sql"))));
            conn.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
