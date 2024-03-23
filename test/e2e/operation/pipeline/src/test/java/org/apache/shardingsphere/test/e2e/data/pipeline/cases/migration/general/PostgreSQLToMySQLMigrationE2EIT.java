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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.general;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@PipelineE2ESettings(fetchSingle = true, database = @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"))
class PostgreSQLToMySQLMigrationE2EIT extends AbstractMigrationE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMySQLToPostgreSQLMigrationSuccess(final PipelineTestParameter testParam) throws SQLException {
        PostgreSQLContainer<?> postgreSQLContainer = null;
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            if (PipelineEnvTypeEnum.DOCKER == PipelineE2EEnvironment.getInstance().getItEnvType()) {
                postgreSQLContainer = new PostgreSQLContainer<>("postgres:13");
                postgreSQLContainer.withNetwork(containerComposer.getContainerComposer().getContainers().getNetwork()).withNetworkAliases("postgresql.host")
                        .withDatabaseName("postgres").withUsername("postgres").withPassword("postgres").withCommand("-c wal_level=logical").start();
            }
            String jdbcUrl = PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.DOCKER ? postgreSQLContainer.getJdbcUrl() : "jdbc:postgresql://localhost:5432/postgres";
            initSourceTable(jdbcUrl);
            registerMigrationSourceStorageUnit(containerComposer);
            containerComposer.registerStorageUnit(PipelineContainerComposer.DS_0);
            containerComposer.proxyExecuteWithLog("CREATE SHARDING TABLE RULE t_order(STORAGE_UNITS(pipeline_it_0),SHARDING_COLUMN=order_id,TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='2')),"
                    + "KEY_GENERATE_STRATEGY(COLUMN=order_id, TYPE(NAME='snowflake')))", 2);
            initTargetTable(containerComposer);
            containerComposer.proxyExecuteWithLog("MIGRATE TABLE source_ds.t_order INTO t_order", 2);
            Awaitility.await().ignoreExceptions().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> !listJobId(containerComposer).isEmpty());
            String jobId = listJobId(containerComposer).get(0);
            containerComposer.waitJobStatusReached(String.format("SHOW MIGRATION STATUS %s", jobId), JobStatus.EXECUTE_INCREMENTAL_TASK, 15);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, "postgres", "postgres")) {
                connection.createStatement().execute(String.format("INSERT INTO t_order (order_id,user_id,status) VALUES (%s, %s, '%s')", "1000000000", 1, "incremental"));
                connection.createStatement().execute(String.format("UPDATE t_order SET status='%s' WHERE order_id IN (1,2)", RandomStringUtils.randomAlphanumeric(10)));
            }
            containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
            assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
            commitMigrationByJobId(containerComposer, jobId);
            List<String> lastJobIds = listJobId(containerComposer);
            assertTrue(lastJobIds.isEmpty());
        } finally {
            if (null != postgreSQLContainer) {
                postgreSQLContainer.close();
            }
        }
    }
    
    private void initSourceTable(final String jdbcUrl) throws SQLException {
        PipelineEnvTypeEnum itEnvType = PipelineE2EEnvironment.getInstance().getItEnvType();
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "postgres", "postgres")) {
            if (PipelineEnvTypeEnum.NATIVE == itEnvType) {
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
        String jdbcUrl = String.format("jdbc:postgresql://%s:5432/postgres",
                PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.DOCKER ? "postgresql.host" : "localhost");
        String sql = String.format("REGISTER MIGRATION SOURCE STORAGE UNIT source_ds (URL='%s', USER='postgres', PASSWORD='postgres')", jdbcUrl);
        containerComposer.proxyExecuteWithLog(sql, 2);
    }
    
    private void initTargetTable(final PipelineContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getProxyDataSource().getConnection()) {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT PRIMARY KEY,user_id INT,status VARCHAR(32), c_datetime DATETIME(6),c_date DATE,c_time TIME,"
                    + "c_bytea BLOB,c_decimal DECIMAL(10,2))");
            connection.createStatement().execute("TRUNCATE TABLE t_order");
        }
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    }
}
