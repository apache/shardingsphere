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
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCase;
import org.apache.shardingsphere.test.integration.framework.container.compose.ComposedContainerRegistry;
import org.apache.shardingsphere.test.integration.framework.container.compose.ComposedContainerRule;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.runner.ShardingSphereIntegrationTestParameterized;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Map;

@RunWith(ShardingSphereIntegrationTestParameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    public static final ComposedContainerRegistry COMPOSED_CONTAINER_REGISTRY = new ComposedContainerRegistry();
    
    private final String scenario;
    
    private final String adapter;
    
    private final DatabaseType databaseType;
    
    private final SQLCommandType sqlCommandType;
    
    private final IntegrationTestCase integrationTestCase;
    
    @Rule
    public final ComposedContainerRule composedContainerRule;
    
    public BaseITCase(final ParameterizedArray parameterizedArray) {
        adapter = parameterizedArray.getAdapter();
        scenario = parameterizedArray.getScenario();
        databaseType = parameterizedArray.getDatabaseType();
        sqlCommandType = parameterizedArray.getSqlCommandType();
        integrationTestCase = parameterizedArray.getTestCaseContext().getTestCase();
        composedContainerRule = new ComposedContainerRule(COMPOSED_CONTAINER_REGISTRY.getComposedContainer(getClass().getSimpleName(), parameterizedArray));
    }
    
    @After
    public void tearDown() throws Exception {
        // TODO Close data sources gracefully.
//        if (targetDataSource instanceof AutoCloseable) {
//            ((AutoCloseable) targetDataSource).close();
//        }
    }
    
    @AfterClass
    public static void releaseResources() {
        // TODO Close current test suite only
        COMPOSED_CONTAINER_REGISTRY.close();
    }
    
    protected final Map<String, DataSource> getActualDataSourceMap() {
        return composedContainerRule.getComposedContainer().getActualDataSourceMap();
    }
    
    protected DataSource getTargetDataSource() {
        return composedContainerRule.getComposedContainer().getTargetDataSource();
    }
    
    protected abstract String getSQL() throws ParseException;
    
    protected void executeUpdateForStatement(final Connection connection, final String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    protected void executeUpdateForPrepareStatement(final Connection connection, final String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }
    
    protected void executeForStatement(final Connection connection, final String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    protected void executeForPrepareStatement(final Connection connection, final String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
        }
    }
}
