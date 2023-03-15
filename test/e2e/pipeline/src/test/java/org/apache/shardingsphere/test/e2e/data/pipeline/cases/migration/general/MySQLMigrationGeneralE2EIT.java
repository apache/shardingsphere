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
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public final class MySQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order_copy";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
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
            DataSourceExecuteUtil.execute(containerComposer.getSourceDataSource(), containerComposer.getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_NAME), dataPair.getLeft());
            DataSourceExecuteUtil.execute(containerComposer.getSourceDataSource(), containerComposer.getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
            log.info("init data end: {}", LocalDateTime.now());
            startMigration(containerComposer, SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
            startMigration(containerComposer, "t_order_item", "t_order_item");
            String orderJobId = getJobIdByTableName(containerComposer, "ds_0." + SOURCE_TABLE_NAME);
            containerComposer.waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", orderJobId));
            containerComposer.startIncrementTask(
                    new E2EIncrementalTask(containerComposer.getSourceDataSource(), SOURCE_TABLE_NAME, new SnowflakeKeyGenerateAlgorithm(), containerComposer.getDatabaseType(), 30));
            assertMigrationSuccessById(containerComposer, orderJobId, "DATA_MATCH");
            String orderItemJobId = getJobIdByTableName(containerComposer, "ds_0.t_order_item");
            assertMigrationSuccessById(containerComposer, orderItemJobId, "DATA_MATCH");
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            assertMigrationSuccessById(containerComposer, orderItemJobId, "CRC32_MATCH");
            for (String each : listJobId(containerComposer)) {
                commitMigrationByJobId(containerComposer, each);
            }
            List<String> lastJobIds = listJobId(containerComposer);
            assertTrue(lastJobIds.isEmpty());
            containerComposer.proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
            containerComposer.assertGreaterThanOrderTableInitRows(PipelineContainerComposer.TABLE_INIT_ROW_COUNT, "");
        }
    }
    
    private void assertMigrationSuccessById(final PipelineContainerComposer containerComposer, final String jobId, final String algorithmType) throws SQLException, InterruptedException {
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
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<Arguments> result = new LinkedList<>();
            MySQLDatabaseType databaseType = new MySQLDatabaseType();
            for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(databaseType)) {
                result.add(Arguments.of(new PipelineTestParameter(databaseType, each, "env/scenario/general/mysql.xml")));
            }
            return result.stream();
        }
    }
}
