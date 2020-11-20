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

package org.apache.shardingsphere.scaling.core.job.task.inventory;

import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.rule.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.junit.Assert.assertFalse;

public final class InventoryDataScalingTaskTest {
    
    private static final String DATA_SOURCE_URL = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    private SyncConfiguration syncConfig;
    
    private DataSourceManager dataSourceManager;
    
    @Before
    public void setUp() {
        DumperConfiguration dumperConfig = mockDumperConfig();
        ImporterConfiguration importerConfig = mockImporterConfig();
        ScalingContext.getInstance().init(new ServerConfiguration());
        syncConfig = new SyncConfiguration(3, dumperConfig, importerConfig);
        dataSourceManager = new DataSourceManager();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test(expected = SyncTaskExecuteException.class)
    public void assertStartWithGetEstimatedRowsFailure() {
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(syncConfig.getDumperConfiguration());
        inventoryDumperConfig.setSourceTable("t_non_exist");
        InventoryDataScalingTask inventoryDataSyncTask = new InventoryDataScalingTask(inventoryDumperConfig, syncConfig.getImporterConfiguration(), dataSourceManager);
        inventoryDataSyncTask.start();
    }
    
    @Test
    public void assertGetProgress() throws SQLException {
        initTableData(syncConfig.getDumperConfiguration());
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(syncConfig.getDumperConfiguration());
        inventoryDumperConfig.setSourceTable("t_order");
        inventoryDumperConfig.setPositionManager(syncConfig.getDumperConfiguration().getPositionManager());
        InventoryDataScalingTask inventoryDataSyncTask = new InventoryDataScalingTask(inventoryDumperConfig, syncConfig.getImporterConfiguration(), dataSourceManager);
        inventoryDataSyncTask.start();
        assertFalse(((InventoryDataSyncTaskProgress) inventoryDataSyncTask.getProgress()).isFinished());
    }
    
    private void initTableData(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private DumperConfiguration mockDumperConfig() {
        DataSourceConfiguration dataSourceConfig = new StandardJDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfiguration(dataSourceConfig);
        result.setPositionManager(new PositionManager(new PrimaryKeyPosition(1, 100)));
        result.setTableNameMap(Collections.emptyMap());
        return result;
    }
    
    private ImporterConfiguration mockImporterConfig() {
        DataSourceConfiguration dataSourceConfig = new StandardJDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfiguration(dataSourceConfig);
        return result;
    }
}
