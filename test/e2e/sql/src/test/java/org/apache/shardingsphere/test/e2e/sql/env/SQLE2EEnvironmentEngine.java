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

package org.apache.shardingsphere.test.e2e.sql.env;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.sql.env.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.sql.env.container.compose.ContainerComposerRegistry;

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
    
    public SQLE2EEnvironmentEngine(final String key, final String scenario, final DatabaseType databaseType, final Mode mode, final Adapter adapter) {
        containerComposer = CONTAINER_COMPOSER_REGISTRY.getContainerComposer(key, scenario, databaseType, mode, adapter);
        containerComposer.start();
        actualDataSourceMap = containerComposer.getActualDataSourceMap();
        targetDataSource = containerComposer.getTargetDataSource();
        expectedDataSourceMap = containerComposer.getExpectedDataSourceMap();
        executeLogicDatabaseInitSQLFileOnlyOnce(key, scenario, databaseType);
    }
    
    private void executeLogicDatabaseInitSQLFileOnlyOnce(final String key, final String scenario, final DatabaseType databaseType) {
        Optional<String> logicDatabaseInitSQLFile = new ScenarioDataPath(scenario, Type.TARGETS).findTargetDatabaseInitSQLFile(DefaultDatabase.LOGIC_NAME, databaseType);
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
