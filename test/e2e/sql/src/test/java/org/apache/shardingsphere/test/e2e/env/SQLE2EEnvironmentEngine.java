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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.env.container.compose.ContainerComposerRegistry;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

/**
 * SQL E2E environment engine.
 */
@Getter
public final class SQLE2EEnvironmentEngine {
    
    private static final ContainerComposerRegistry CONTAINER_COMPOSER_REGISTRY = new ContainerComposerRegistry();
    
    private static final Collection<String> INITIALIZED_SUITES = new HashSet<>();
    
    @Getter(AccessLevel.NONE)
    private final ContainerComposer containerComposer;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final DataSource targetDataSource;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    public SQLE2EEnvironmentEngine(final String key, final String scenario, final DatabaseType databaseType, final AdapterMode adapterMode, final AdapterType adapterType) {
        containerComposer = CONTAINER_COMPOSER_REGISTRY.getContainerComposer(key, scenario, databaseType, adapterMode, adapterType);
        containerComposer.start();
        actualDataSourceMap = containerComposer.getActualDataSourceMap();
        targetDataSource = containerComposer.getTargetDataSource();
        expectedDataSourceMap = containerComposer.getExpectedDataSourceMap();
        executeLogicDatabaseInitSQLFileOnlyOnce(key, scenario, databaseType);
    }
    
    private void executeLogicDatabaseInitSQLFileOnlyOnce(final String key, final String scenario, final DatabaseType databaseType) {
        Optional<String> logicDatabaseInitSQLFile = new ScenarioDataPath(scenario).findActualDatabaseInitSQLFile(DefaultDatabase.LOGIC_NAME, databaseType);
        if (!logicDatabaseInitSQLFile.isPresent()) {
            return;
        }
        String cacheKey = key + "-" + System.identityHashCode(targetDataSource);
        if (!INITIALIZED_SUITES.contains(cacheKey)) {
            synchronized (INITIALIZED_SUITES) {
                if (!INITIALIZED_SUITES.contains(cacheKey)) {
                    SQLScriptUtils.execute(targetDataSource, logicDatabaseInitSQLFile.get());
                    INITIALIZED_SUITES.add(cacheKey);
                }
            }
        }
    }
}
