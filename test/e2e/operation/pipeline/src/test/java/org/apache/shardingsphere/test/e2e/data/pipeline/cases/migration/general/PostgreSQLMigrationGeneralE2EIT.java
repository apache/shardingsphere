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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtils;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(database = {
        @PipelineE2EDatabaseSettings(type = "PostgreSQL", scenarioFiles = "env/scenario/general/postgresql.xml"),
        @PipelineE2EDatabaseSettings(type = "openGauss", scenarioFiles = "env/scenario/general/opengauss.xml")})
@Slf4j
class PostgreSQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            addMigrationProcessConfig(containerComposer);
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
            log.info("init data end: {}", LocalDateTime.now());
            startMigrationWithSchema(containerComposer, SOURCE_TABLE_NAME, "t_order");
            Awaitility.await().atMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> listJobId(containerComposer).size() > 0);
            String jobId = getJobIdByTableName(containerComposer, "ds_0.test." + SOURCE_TABLE_NAME);
            containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
            String schemaTableName = String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME);
            containerComposer.startIncrementTask(new E2EIncrementalTask(containerComposer.getSourceDataSource(), schemaTableName, new SnowflakeKeyGenerateAlgorithm(),
                    containerComposer.getDatabaseType(), 20));
            TimeUnit.SECONDS.timedJoin(containerComposer.getIncreaseTaskThread(), 30);
            containerComposer.sourceExecuteWithLog(String.format("INSERT INTO %s (order_id, user_id, status) VALUES (10000, 1, 'OK')", schemaTableName));
            DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            containerComposer.assertOrderRecordExist(jdbcDataSource, schemaTableName, 10000);
            checkOrderMigration(containerComposer, jobId);
            checkOrderItemMigration(containerComposer);
            for (String each : listJobId(containerComposer)) {
                commitMigrationByJobId(containerComposer, each);
            }
            List<String> lastJobIds = listJobId(containerComposer);
            assertTrue(lastJobIds.isEmpty());
            containerComposer.assertGreaterThanOrderTableInitRows(jdbcDataSource, PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1, PipelineContainerComposer.SCHEMA_NAME);
        }
    }
    
    private void checkOrderMigration(final PipelineContainerComposer containerComposer, final String jobId) throws SQLException {
        containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        stopMigrationByJobId(containerComposer, jobId);
        long recordId = new SnowflakeKeyGenerateAlgorithm().generateKey();
        containerComposer.sourceExecuteWithLog(String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')",
                String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME), recordId, 1, "afterStop"));
        startMigrationByJobId(containerComposer, jobId);
        DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
        containerComposer.assertOrderRecordExist(jdbcDataSource, String.join(".", PipelineContainerComposer.SCHEMA_NAME, TARGET_TABLE_NAME), recordId);
        assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
    }
    
    private void checkOrderItemMigration(final PipelineContainerComposer containerComposer) throws SQLException {
        startMigrationWithSchema(containerComposer, "t_order_item", "t_order_item");
        String jobId = getJobIdByTableName(containerComposer, "ds_0.test.t_order_item");
        containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(new PostgreSQLDatabaseType(), new OpenGaussDatabaseType());
    }
}
