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
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCase;
import org.apache.shardingsphere.test.integration.framework.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.framework.compose.mode.ClusterComposedContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.runner.ShardingSphereIntegrationTestParameterized;
import org.junit.After;
import org.junit.Before;
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
    
    @Rule
    public final ComposedContainer composedContainer;
    
    private final String adapter;
    
    private final String scenario;
    
    private final DatabaseType databaseType;
    
    private final SQLCommandType sqlCommandType;
    
    private final IntegrationTestCase integrationTestCase;
    
    private final ShardingSphereStorageContainer storageContainer;
    
    private final ShardingSphereAdapterContainer adapterContainer;
    
    private Map<String, DataSource> dataSourceMap;
    
    private DataSource targetDataSource;
    
    private DataSource dataSourceForReader;
    
    public BaseITCase(final ParameterizedArray parameterizedArray) {
        adapter = parameterizedArray.getAdapter();
        composedContainer = parameterizedArray.getCompose();
        scenario = parameterizedArray.getScenario();
        databaseType = parameterizedArray.getDatabaseType();
        sqlCommandType = parameterizedArray.getSqlCommandType();
        storageContainer = composedContainer.getStorageContainer();
        adapterContainer = composedContainer.getAdapterContainer();
        integrationTestCase = parameterizedArray.getTestCaseContext().getTestCase();
    }
    
    @Before
    public void init() throws Exception {
        dataSourceMap = composedContainer.getDataSourceMap();
        targetDataSource = dataSourceMap.get("adapterForWriter");
        if (composedContainer instanceof ClusterComposedContainer) {
            dataSourceForReader = dataSourceMap.get("adapterForReader");
            int waitForGov = 10;
            while (waitForGov-- > 0) {
                try (Connection ignored = targetDataSource.getConnection()) {
                    return;
                } catch (NullPointerException ignored) {
                    Thread.sleep(2000L);
                }
            }
        }
    }
    
    @After
    public void tearDown() throws Exception {
        // TODO Closing data sources gracefully.
//        if (targetDataSource instanceof ShardingSphereDataSource) {
//            closeDataSource(((ShardingSphereDataSource) targetDataSource));
//        }
//        if (null != dataSourceForReader && dataSourceForReader instanceof ShardingSphereDataSource) {
//            closeDataSource(((ShardingSphereDataSource) dataSourceForReader));
//        }
    }
    
    private void closeDataSource(final ShardingSphereDataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("SELECT 1");
        }
        dataSource.getContextManager().close();
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
