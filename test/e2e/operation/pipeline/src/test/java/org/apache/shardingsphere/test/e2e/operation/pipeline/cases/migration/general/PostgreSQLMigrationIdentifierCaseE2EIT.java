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
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@PipelineE2ESettings(database = @PipelineE2EDatabaseSettings(type = "PostgreSQL"))
class PostgreSQLMigrationIdentifierCaseE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_SCHEMA_NAME = "UPPER_SCHEMA";
    
    private static final String SOURCE_TABLE_NAME = "UPPER_TABLE";
    
    private static final String TARGET_TABLE_NAME = "upper_schema.t_order";
    
    private static final long INVENTORY_ORDER_ID = 1L;
    
    private static final long INCREMENTAL_ORDER_ID = 2L;
    
    private static final long LOWER_CASE_CONFLICT_ORDER_ID = 20001L;
    
    private static final long CROSS_SCHEMA_CONFLICT_ORDER_ID = 20002L;
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationWithUpperCaseSourceIdentifiers(final PipelineTestParameter testParam) throws SQLException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            distSQLFacade.alterPipelineRule();
            createSourceTables(containerComposer);
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetOrderTableRule(containerComposer);
            insertSourceRecord(containerComposer, qualifiedUpperCaseTable(), INVENTORY_ORDER_ID);
            int replicationSlotsCount = getReplicationSlotsCount(containerComposer);
            log.info("Start migration at {}, replication slots count: {}", LocalDateTime.now(), replicationSlotsCount);
            containerComposer.proxyExecuteWithLog("MIGRATE TABLE ds_0.`UPPER_SCHEMA`.`UPPER_TABLE` INTO sharding_DB.T_ORDER", 5);
            Awaitility.waitAtMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !distSQLFacade.listJobIds().isEmpty());
            String jobId = distSQLFacade.getJobIdByTableName("ds_0.UPPER_SCHEMA.UPPER_TABLE");
            distSQLFacade.waitJobPreparingStageFinished(jobId);
            distSQLFacade.waitJobIncrementalStageStarted(jobId);
            insertSourceRecord(containerComposer, qualifiedUpperCaseTable(), INCREMENTAL_ORDER_ID);
            insertSourceRecord(containerComposer, "upper_schema.upper_table", LOWER_CASE_CONFLICT_ORDER_ID);
            insertSourceRecord(containerComposer, "public.\"UPPER_TABLE\"", CROSS_SCHEMA_CONFLICT_ORDER_ID);
            assertFalse(distSQLFacade.waitJobIncrementalStageFinished(jobId).isEmpty(), "Migration incremental stage did not become idle");
            DataSource targetDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            containerComposer.assertRecordExists(targetDataSource, TARGET_TABLE_NAME, INVENTORY_ORDER_ID);
            containerComposer.assertRecordExists(targetDataSource, TARGET_TABLE_NAME, INCREMENTAL_ORDER_ID);
            assertRecordAbsent(targetDataSource, LOWER_CASE_CONFLICT_ORDER_ID);
            assertRecordAbsent(targetDataSource, CROSS_SCHEMA_CONFLICT_ORDER_ID);
            distSQLFacade.startCheckAndVerify(jobId, "DATA_MATCH");
            distSQLFacade.commit(jobId);
            assertTrue(distSQLFacade.listJobIds().isEmpty());
            Awaitility.waitAtMost(30L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS)
                    .until(() -> getReplicationSlotsCount(containerComposer) == replicationSlotsCount);
        }
    }
    
    private void createSourceTables(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.sourceExecuteWithLog("CREATE SCHEMA IF NOT EXISTS \"UPPER_SCHEMA\"");
        containerComposer.sourceExecuteWithLog("CREATE SCHEMA IF NOT EXISTS upper_schema");
        containerComposer.sourceExecuteWithLog(createTableSQL(qualifiedUpperCaseTable(), "\"ORDER_ID\"", "\"USER_ID\"", "\"STATUS\""));
        containerComposer.sourceExecuteWithLog(createTableSQL("upper_schema.upper_table", "order_id", "user_id", "status"));
        containerComposer.sourceExecuteWithLog(createTableSQL("public.\"UPPER_TABLE\"", "\"ORDER_ID\"", "\"USER_ID\"", "\"STATUS\""));
    }
    
    private String createTableSQL(final String tableName, final String orderIdColumn, final String userIdColumn, final String statusColumn) {
        return String.format("CREATE TABLE %s (%s BIGINT PRIMARY KEY, %s INT NOT NULL, %s VARCHAR(32) NOT NULL)", tableName, orderIdColumn, userIdColumn, statusColumn);
    }
    
    private void insertSourceRecord(final PipelineContainerComposer containerComposer, final String tableName, final long orderId) throws SQLException {
        containerComposer.sourceExecuteWithLog(String.format("INSERT INTO %s VALUES (%d, 1, 'OK')", tableName, orderId));
    }
    
    private void assertRecordAbsent(final DataSource dataSource, final long orderId) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("SELECT 1 FROM %s WHERE order_id = %d", TARGET_TABLE_NAME, orderId))) {
            assertFalse(resultSet.next(), "A record from a conflicting source table must not be migrated");
        }
    }
    
    private int getReplicationSlotsCount(final PipelineContainerComposer containerComposer) throws SQLException {
        try (
                Connection connection = containerComposer.getSourceDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM pg_replication_slots")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
    
    private String qualifiedUpperCaseTable() {
        return String.format("\"%s\".\"%s\"", SOURCE_SCHEMA_NAME, SOURCE_TABLE_NAME);
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
