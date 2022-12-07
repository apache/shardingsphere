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

package org.apache.shardingsphere.test.e2e.engine;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.cases.assertion.IntegrationTestCase;
import org.apache.shardingsphere.test.e2e.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.container.compose.ContainerComposerRegistry;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;
import org.apache.shardingsphere.test.e2e.framework.runner.ParallelParameterized;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;

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

@RunWith(ParallelParameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseE2EIT {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    private static final ContainerComposerRegistry CONTAINER_COMPOSER_REGISTRY = new ContainerComposerRegistry();
    
    private static final int TOTAL_SUITES_COUNT = TotalSuitesCountCalculator.calculate();
    
    private static final AtomicInteger COMPLETED_SUITES_COUNT = new AtomicInteger(0);
    
    private static final Collection<String> INITIALIZED_SUITES = new HashSet<>();
    
    private final String mode;
    
    private final String scenario;
    
    private final DatabaseType databaseType;
    
    private final String itKey;
    
    private final IntegrationTestCase itCase;
    
    @Getter(AccessLevel.NONE)
    private final ContainerComposer containerComposer;
    
    private Map<String, DataSource> actualDataSourceMap;
    
    private DataSource targetDataSource;
    
    private Map<String, DataSource> expectedDataSourceMap;
    
    public BaseE2EIT(final E2ETestParameter testParam) {
        mode = testParam.getMode();
        scenario = testParam.getScenario();
        databaseType = testParam.getDatabaseType();
        itKey = testParam.getKey();
        itCase = testParam.getTestCaseContext().getTestCase();
        containerComposer = CONTAINER_COMPOSER_REGISTRY.getContainerComposer(testParam);
    }
    
    @Before
    public void setUp() throws SQLException, IOException {
        containerComposer.start();
        actualDataSourceMap = containerComposer.getActualDataSourceMap();
        targetDataSource = containerComposer.getTargetDataSource();
        expectedDataSourceMap = containerComposer.getExpectedDataSourceMap();
        executeLogicDatabaseInitSQLFileOnlyOnce(targetDataSource);
    }
    
    private void executeLogicDatabaseInitSQLFileOnlyOnce(final DataSource targetDataSource) throws SQLException, IOException {
        Optional<String> logicDatabaseInitSQLFile = new ScenarioDataPath(getScenario()).findActualDatabaseInitSQLFile(DefaultDatabase.LOGIC_NAME, getDatabaseType());
        if (!logicDatabaseInitSQLFile.isPresent()) {
            return;
        }
        if (!INITIALIZED_SUITES.contains(getItKey())) {
            synchronized (INITIALIZED_SUITES) {
                if (!INITIALIZED_SUITES.contains(getItKey())) {
                    executeInitSQL(targetDataSource, logicDatabaseInitSQLFile.get());
                    INITIALIZED_SUITES.add(getItKey());
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
    
    @AfterClass
    public static void closeContainers() {
        if (COMPLETED_SUITES_COUNT.incrementAndGet() == TOTAL_SUITES_COUNT) {
            CONTAINER_COMPOSER_REGISTRY.close();
        }
    }
}
