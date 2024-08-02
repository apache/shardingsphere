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

package org.apache.shardingsphere.test.e2e.env;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.container.compose.ContainerComposerRegistry;
import org.apache.shardingsphere.test.e2e.engine.TotalSuitesCountCalculator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * E2E container composer.
 */
@Getter
public final class E2EEnvironmentEngine {
    
    private static final ContainerComposerRegistry CONTAINER_COMPOSER_REGISTRY = new ContainerComposerRegistry();
    
    private static final int TOTAL_SUITES_COUNT = TotalSuitesCountCalculator.calculate();
    
    private static final AtomicInteger COMPLETED_SUITES_COUNT = new AtomicInteger(0);
    
    private static final Collection<String> INITIALIZED_SUITES = new HashSet<>();
    
    private final ContainerComposer containerComposer;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final DataSource targetDataSource;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    public E2EEnvironmentEngine(final String key, final String scenario, final DatabaseType databaseType, final AdapterMode adapterMode, final AdapterType adapterType) {
        containerComposer = CONTAINER_COMPOSER_REGISTRY.getContainerComposer(key, scenario, databaseType, adapterMode, adapterType);
        containerComposer.start();
        actualDataSourceMap = containerComposer.getActualDataSourceMap();
        targetDataSource = containerComposer.getTargetDataSource();
        expectedDataSourceMap = containerComposer.getExpectedDataSourceMap();
        executeLogicDatabaseInitSQLFileOnlyOnce(key, scenario, databaseType, targetDataSource);
    }
    
    @SneakyThrows({SQLException.class, IOException.class})
    private void executeLogicDatabaseInitSQLFileOnlyOnce(final String key, final String scenario, final DatabaseType databaseType, final DataSource targetDataSource) {
        Optional<String> logicDatabaseInitSQLFile = new ScenarioDataPath(scenario).findActualDatabaseInitSQLFile(DefaultDatabase.LOGIC_NAME, databaseType);
        if (!logicDatabaseInitSQLFile.isPresent()) {
            return;
        }
        String cacheKey = key + "-" + System.identityHashCode(targetDataSource);
        if (!INITIALIZED_SUITES.contains(cacheKey)) {
            synchronized (INITIALIZED_SUITES) {
                if (!INITIALIZED_SUITES.contains(cacheKey)) {
                    executeInitSQL(targetDataSource, logicDatabaseInitSQLFile.get());
                    INITIALIZED_SUITES.add(cacheKey);
                }
            }
        }
    }
    
    private void executeInitSQL(final DataSource dataSource, final String logicDatabaseInitSQLFile) throws SQLException, IOException {
        try (
                Connection connection = dataSource.getConnection();
                FileReader reader = new FileReader(logicDatabaseInitSQLFile)) {
            RunScript.execute(connection, reader);
        }
    }
}
