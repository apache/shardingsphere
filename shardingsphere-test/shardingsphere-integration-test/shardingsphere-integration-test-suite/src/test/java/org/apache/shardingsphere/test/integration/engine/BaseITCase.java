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

package org.apache.shardingsphere.test.integration.engine;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCase;
import org.apache.shardingsphere.test.integration.container.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.container.compose.ComposedContainerRegistry;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.runner.ShardingSphereIntegrationTestParameterized;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(ShardingSphereIntegrationTestParameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    private static final ComposedContainerRegistry COMPOSED_CONTAINER_REGISTRY = new ComposedContainerRegistry();
    
    private static final int TOTAL_SUITES_COUNT = TotalSuitesCountCalculator.calculate();
    
    private static final AtomicInteger COMPLETED_SUITES_COUNT = new AtomicInteger(0);
    
    private final String mode;
    
    private final String scenario;
    
    private final DatabaseType databaseType;
    
    private final String itKey;
    
    private final IntegrationTestCase itCase;
    
    @Getter(AccessLevel.NONE)
    private final ComposedContainer composedContainer;
    
    private Map<String, DataSource> actualDataSourceMap;
    
    private DataSource targetDataSource;
    
    private Map<String, DataSource> expectedDataSourceMap;
    
    public BaseITCase(final ParameterizedArray parameterizedArray) {
        mode = parameterizedArray.getMode();
        scenario = parameterizedArray.getScenario();
        databaseType = parameterizedArray.getDatabaseType();
        itKey = parameterizedArray.getKey();
        itCase = parameterizedArray.getTestCaseContext().getTestCase();
        composedContainer = COMPOSED_CONTAINER_REGISTRY.getComposedContainer(parameterizedArray);
    }
    
    @Before
    public void setUp() {
        composedContainer.start();
        actualDataSourceMap = composedContainer.getActualDataSourceMap();
        targetDataSource = composedContainer.getTargetDataSource();
        expectedDataSourceMap = composedContainer.getExpectedDataSourceMap();
    }
    
    @AfterClass
    public static void closeContainers() {
        if (COMPLETED_SUITES_COUNT.incrementAndGet() == TOTAL_SUITES_COUNT) {
            COMPOSED_CONTAINER_REGISTRY.close();
        }
    }
}
