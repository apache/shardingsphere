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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.general;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(fetchSingle = true, database = @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"))
@Slf4j
class PostgreSQLToMySQLMigrationE2EIT extends AbstractMigrationE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException {
        PostgreSQLContainer<?> postgresqlContainer = null;
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            if (Type.DOCKER == type) {
                postgresqlContainer = new PostgreSQLContainer<>("postgres:13");
                postgresqlContainer.withNetwork(containerComposer.getContainerComposer().getContainers().getNetwork()).withNetworkAliases("postgresql.host")
                        .withDatabaseName("postgres").withUsername("postgres").withPassword("postgres").withCommand("-c wal_level=logical").start();
            }
            String jdbcUrl = Type.DOCKER == type ? postgresqlContainer.getJdbcUrl() : "jdbc:postgresql://localhost:5432/postgres";
            initSourceTable(jdbcUrl);
            registerMigrationSourceStorageUnit(containerComposer);
            containerComposer.registerStorageUnit(PipelineContainerComposer.DS_0);
            containerComposer.proxyExecuteWithLog("CREATE SHARDING TABLE RULE t_order(STORAGE_UNITS(pipeline_e2e_0),SHARDING_COLUMN=order_id,TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='2')),"
                    + "KEY_GENERATE_STRATEGY(COLUMN=order_id, TYPE(NAME='snowflake')))", 2);
            initTargetTable(containerComposer);
            containerComposer.proxyExecuteWithLog("MIGRATE TABLE source_ds.t_order INTO t_order", 2);
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            Awaitility.await().ignoreExceptions().atMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !distSQLFacade.listJobIds().isEmpty());
            String jobId = distSQLFacade.listJobIds().get(0);
            distSQLFacade.waitJobStatusReached(jobId, JobStatus.EXECUTE_INCREMENTAL_TASK, 15);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, "postgres", "postgres")) {
                connection.createStatement().execute(String.format("INSERT INTO t_order (order_id,user_id,status) VALUES (%s, %s, '%s')", "1000000000", 1, "incremental"));
                connection.createStatement().execute(String.format("UPDATE t_order SET status='%s' WHERE order_id IN (1,2)", RandomStringUtils.randomAlphanumeric(10)));
            }
            distSQLFacade.waitIncrementTaskFinished(jobId);
            startCheckAndVerify(containerComposer, jobId, "DATA_MATCH");
            distSQLFacade.commit(jobId);
            assertTrue(distSQLFacade.listJobIds().isEmpty());
        } finally {
            if (null != postgresqlContainer) {
                postgresqlContainer.close();
            }
        }
    }
    
    private void initSourceTable(final String jdbcUrl) throws SQLException {
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "postgres", "postgres")) {
            if (Type.NATIVE == type) {
                connection.createStatement().execute("DROP TABLE IF EXISTS t_order;");
            }
            String createTableSQL = "CREATE TABLE t_order (order_id BIGINT PRIMARY KEY,user_id INT,status VARCHAR(32), c_datetime TIMESTAMP,c_date DATE,c_time TIME,c_bytea BYTEA,"
                    + "c_decimal DECIMAL(10,2))";
            log.info("createTableSQL: {}", createTableSQL);
            connection.createStatement().execute(createTableSQL);
            String insertSQL = "INSERT INTO t_order (order_id,user_id,status,c_datetime,c_date,c_time,c_bytea,c_decimal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            for (int i = 1; i <= 10; i++) {
                preparedStatement.setObject(1, i);
                preparedStatement.setObject(2, i + 10);
                preparedStatement.setObject(3, RandomStringUtils.randomAlphanumeric(10));
                preparedStatement.setObject(4, LocalDateTime.now());
                preparedStatement.setObject(5, LocalDate.now());
                preparedStatement.setObject(6, LocalTime.now().withNano(0));
                preparedStatement.setObject(7, new byte[]{1, 2, 3, 4, 5});
                preparedStatement.setObject(8, new BigDecimal(i * RandomUtils.nextInt(1, 100) + ".22"));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    private void registerMigrationSourceStorageUnit(final PipelineContainerComposer containerComposer) throws SQLException {
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        if (Type.NATIVE == type) {
            try {
                containerComposer.proxyExecuteWithLog("UNREGISTER MIGRATION SOURCE STORAGE UNIT source_ds", 2);
            } catch (final SQLException ex) {
                log.warn("Unregister migration source storage unit `source_ds` failed, maybe it does not exist. Error msg: {}", ex.getMessage());
            }
        }
        String jdbcUrl = String.format("jdbc:postgresql://%s:5432/postgres", Type.DOCKER == type ? "postgresql.host" : "localhost");
        String sql = String.format("REGISTER MIGRATION SOURCE STORAGE UNIT source_ds (URL='%s', USER='postgres', PASSWORD='postgres')", jdbcUrl);
        containerComposer.proxyExecuteWithLog(sql, 2);
    }
    
    private void initTargetTable(final PipelineContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getProxyDataSource().getConnection()) {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT PRIMARY KEY,user_id INT,status VARCHAR(32), c_datetime DATETIME(6),c_date DATE,c_time TIME,"
                    + "c_bytea BLOB,c_decimal DECIMAL(10,2))");
            if (waitForTableExistence(connection, "t_order")) {
                connection.createStatement().execute("TRUNCATE TABLE t_order");
            } else {
                throw new SQLException("Table t_order does not exist");
            }
        }
    }
    
    private static boolean waitForTableExistence(final Connection connection, final String tableName) {
        try {
            Awaitility.await().ignoreExceptions().atMost(60L, TimeUnit.SECONDS).pollInterval(3L, TimeUnit.SECONDS).until(() -> tableExists(connection, tableName));
            return true;
        } catch (final ConditionTimeoutException ex) {
            return false;
        }
    }
    
    private static boolean tableExists(final Connection connection, final String tableName) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
