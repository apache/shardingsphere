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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.algorithm.keygen.snowflake.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(database = {
        @PipelineE2EDatabaseSettings(type = "PostgreSQL", scenarioFiles = "env/scenario/general/postgresql.xml"),
        @PipelineE2EDatabaseSettings(type = "openGauss", scenarioFiles = "env/scenario/general/opengauss.xml")})
@Slf4j
class PostgreSQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            distSQLFacade.alterPipelineRule();
            createSourceSchema(containerComposer, PipelineContainerComposer.SCHEMA_NAME);
            containerComposer.createSourceOrderTable(SOURCE_TABLE_NAME);
            containerComposer.createSourceOrderItemTable();
            containerComposer.createSourceTableIndexList(PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME);
            containerComposer.createSourceCommentOnList(PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME);
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetOrderTableRule(containerComposer);
            createTargetOrderItemTableRule(containerComposer);
            Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(containerComposer.getDatabaseType(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            log.info("init data begin: {}", LocalDateTime.now());
            DataSourceExecuteUtils.execute(containerComposer.getSourceDataSource(), containerComposer.getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_NAME), dataPair.getLeft());
            DataSourceExecuteUtils.execute(containerComposer.getSourceDataSource(), containerComposer.getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
            int replicationSlotsCount = getReplicationSlotsCount(containerComposer);
            log.info("init data end: {}, replication slots count: {}", LocalDateTime.now(), replicationSlotsCount);
            startMigrationWithSchema(containerComposer, SOURCE_TABLE_NAME, "t_order");
            Awaitility.await().atMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !distSQLFacade.listJobIds().isEmpty());
            String jobId = distSQLFacade.getJobIdByTableName("ds_0.test." + SOURCE_TABLE_NAME);
            distSQLFacade.waitJobPreparingStageFinished(jobId);
            String qualifiedTableName = String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME);
            containerComposer.startIncrementTask(new E2EIncrementalTask(containerComposer.getSourceDataSource(), qualifiedTableName, new SnowflakeKeyGenerateAlgorithm(),
                    containerComposer.getDatabaseType(), 20));
            TimeUnit.SECONDS.timedJoin(containerComposer.getIncreaseTaskThread(), 30L);
            containerComposer.sourceExecuteWithLog(String.format("INSERT INTO %s (order_id, user_id, status) VALUES (10000, 1, 'OK')", qualifiedTableName));
            DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            containerComposer.assertOrderRecordExist(jdbcDataSource, qualifiedTableName, 10000);
            checkOrderMigration(distSQLFacade, jobId);
            startMigrationWithSchema(containerComposer, "t_order_item", "t_order_item");
            checkOrderItemMigration(distSQLFacade);
            for (String each : distSQLFacade.listJobIds()) {
                distSQLFacade.commit(each);
            }
            assertTrue(distSQLFacade.listJobIds().isEmpty());
            containerComposer.assertGreaterThanOrderTableInitRows(jdbcDataSource, PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1, PipelineContainerComposer.SCHEMA_NAME);
            assertThat("Replication slots count doesn't match, it might be not cleaned, run `SELECT * FROM pg_replication_slots;` in PostgreSQL to verify",
                    getReplicationSlotsCount(containerComposer), is(replicationSlotsCount));
        }
    }
    
    private void checkOrderMigration(final PipelineE2EDistSQLFacade distSQLFacade, final String jobId) throws SQLException {
        distSQLFacade.waitJobIncrementalStageFinished(jobId);
        distSQLFacade.pauseJob(jobId);
        distSQLFacade.resumeJob(jobId);
        distSQLFacade.startCheckAndVerify(jobId, "DATA_MATCH");
    }
    
    private void checkOrderItemMigration(final PipelineE2EDistSQLFacade distSQLFacade) throws SQLException {
        String jobId = distSQLFacade.getJobIdByTableName("ds_0.test.t_order_item");
        distSQLFacade.waitJobIncrementalStageStarted(jobId);
        distSQLFacade.startCheckAndVerify(jobId, "DATA_MATCH");
    }
    
    private int getReplicationSlotsCount(final PipelineContainerComposer containerComposer) throws SQLException {
        try (
                Connection connection = containerComposer.getSourceDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM pg_replication_slots")) {
            if (!resultSet.next()) {
                return 0;
            }
            return resultSet.getInt(1);
        }
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
