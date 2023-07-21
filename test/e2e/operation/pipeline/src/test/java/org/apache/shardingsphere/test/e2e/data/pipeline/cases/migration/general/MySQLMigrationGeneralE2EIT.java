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
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
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
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(database = @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/scenario/general/mysql.xml"))
@Slf4j
class MySQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order_copy";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            addMigrationProcessConfig(containerComposer);
            containerComposer.createSourceOrderTable(SOURCE_TABLE_NAME);
            containerComposer.createSourceOrderItemTable();
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetOrderTableRule(containerComposer);
            createTargetOrderTableEncryptRule(containerComposer);
            createTargetOrderItemTableRule(containerComposer);
            Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(containerComposer.getDatabaseType(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            log.info("init data begin: {}", LocalDateTime.now());
            DataSourceExecuteUtils.execute(containerComposer.getSourceDataSource(), containerComposer.getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_NAME), dataPair.getLeft());
            DataSourceExecuteUtils.execute(containerComposer.getSourceDataSource(), containerComposer.getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
            log.info("init data end: {}", LocalDateTime.now());
            startMigration(containerComposer, SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
            startMigration(containerComposer, "t_order_item", "t_order_item");
            String orderJobId = getJobIdByTableName(containerComposer, "ds_0." + SOURCE_TABLE_NAME);
            containerComposer.waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", orderJobId));
            containerComposer.startIncrementTask(
                    new E2EIncrementalTask(containerComposer.getSourceDataSource(), SOURCE_TABLE_NAME, new SnowflakeKeyGenerateAlgorithm(), containerComposer.getDatabaseType(), 30));
            TimeUnit.SECONDS.timedJoin(containerComposer.getIncreaseTaskThread(), 30);
            containerComposer.sourceExecuteWithLog(String.format("INSERT INTO %s (order_id, user_id, status) VALUES (10000, 1, 'OK')", SOURCE_TABLE_NAME));
            DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            containerComposer.assertOrderRecordExist(jdbcDataSource, "t_order", 10000);
            assertMigrationSuccessById(containerComposer, orderJobId, "DATA_MATCH");
            String orderItemJobId = getJobIdByTableName(containerComposer, "ds_0.t_order_item");
            assertMigrationSuccessById(containerComposer, orderItemJobId, "DATA_MATCH");
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
            assertMigrationSuccessById(containerComposer, orderItemJobId, "CRC32_MATCH");
            for (String each : listJobId(containerComposer)) {
                commitMigrationByJobId(containerComposer, each);
            }
            List<String> lastJobIds = listJobId(containerComposer);
            assertTrue(lastJobIds.isEmpty());
            containerComposer.assertGreaterThanOrderTableInitRows(jdbcDataSource, PipelineContainerComposer.TABLE_INIT_ROW_COUNT, "");
        }
    }
    
    private void assertMigrationSuccessById(final PipelineContainerComposer containerComposer, final String jobId, final String algorithmType) throws SQLException {
        List<Map<String, Object>> jobStatus = containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        for (Map<String, Object> each : jobStatus) {
            assertTrue(Integer.parseInt(each.get("processed_records_count").toString()) > 0);
            assertThat(Integer.parseInt(each.get("inventory_finished_percentage").toString()), is(100));
        }
        assertCheckMigrationSuccess(containerComposer, jobId, algorithmType);
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(new MySQLDatabaseType());
    }
}
