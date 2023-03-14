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
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(Parameterized.class)
@Slf4j
public final class MySQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_ORDER_NAME = "t_order_copy";
    
    public MySQLMigrationGeneralE2EIT(final PipelineTestParameter testParam) {
        super(testParam, new MigrationJobType());
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(databaseType)) {
            result.add(new PipelineTestParameter(databaseType, each, "env/scenario/general/mysql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertMigrationSuccess() throws SQLException, InterruptedException {
        addMigrationProcessConfig();
        getContainerComposer().createSourceOrderTable(SOURCE_TABLE_ORDER_NAME);
        getContainerComposer().createSourceOrderItemTable();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        createTargetOrderTableEncryptRule();
        createTargetOrderItemTableRule();
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(getContainerComposer().getDatabaseType(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(getContainerComposer().getSourceDataSource(), getContainerComposer().getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_ORDER_NAME), dataPair.getLeft());
        DataSourceExecuteUtil.execute(getContainerComposer().getSourceDataSource(), getContainerComposer().getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        log.info("init data end: {}", LocalDateTime.now());
        startMigration(SOURCE_TABLE_ORDER_NAME, getContainerComposer().getTargetTableOrderName());
        startMigration("t_order_item", "t_order_item");
        String orderJobId = getJobIdByTableName("ds_0." + SOURCE_TABLE_ORDER_NAME);
        getContainerComposer().waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", orderJobId));
        getContainerComposer().startIncrementTask(
                new E2EIncrementalTask(getContainerComposer().getSourceDataSource(), SOURCE_TABLE_ORDER_NAME, new SnowflakeKeyGenerateAlgorithm(), getContainerComposer().getDatabaseType(), 30));
        assertMigrationSuccessById(orderJobId, "DATA_MATCH");
        String orderItemJobId = getJobIdByTableName("ds_0.t_order_item");
        assertMigrationSuccessById(orderItemJobId, "DATA_MATCH");
        ThreadUtil.sleep(2, TimeUnit.SECONDS);
        assertMigrationSuccessById(orderItemJobId, "CRC32_MATCH");
        for (String each : listJobId()) {
            commitMigrationByJobId(each);
        }
        List<String> lastJobIds = listJobId();
        assertTrue(lastJobIds.isEmpty());
        getContainerComposer().proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        getContainerComposer().assertGreaterThanOrderTableInitRows(PipelineContainerComposer.TABLE_INIT_ROW_COUNT, "");
    }
    
    private void assertMigrationSuccessById(final String jobId, final String algorithmType) throws SQLException, InterruptedException {
        List<Map<String, Object>> jobStatus = getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        for (Map<String, Object> each : jobStatus) {
            assertTrue(Integer.parseInt(each.get("processed_records_count").toString()) > 0);
            assertThat(Integer.parseInt(each.get("inventory_finished_percentage").toString()), is(100));
        }
        assertCheckMigrationSuccess(jobId, algorithmType);
    }
}
