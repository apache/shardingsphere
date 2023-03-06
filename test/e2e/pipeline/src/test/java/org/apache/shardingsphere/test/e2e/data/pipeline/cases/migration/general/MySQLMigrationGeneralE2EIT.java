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
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.MySQLIncrementTask;
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
    
    private final PipelineTestParameter testParam;
    
    public MySQLMigrationGeneralE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
        this.testParam = testParam;
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String each : PipelineBaseE2EIT.ENV.listStorageContainerImages(databaseType)) {
            result.add(new PipelineTestParameter(databaseType, each, "env/scenario/general/mysql.xml"));
        }
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order_copy";
    }
    
    @Test
    public void assertMigrationSuccess() throws SQLException, InterruptedException {
        log.info("assertMigrationSuccess testParam:{}", testParam);
        initEnvironment(testParam.getDatabaseType(), new MigrationJobType());
        addMigrationProcessConfig();
        createSourceOrderTable();
        createSourceOrderItemTable();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        createTargetOrderTableEncryptRule();
        createTargetOrderItemTableRule();
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(testParam.getDatabaseType(), PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(getSourceDataSource(), getExtraSQLCommand().getFullInsertOrder(getSourceTableOrderName()), dataPair.getLeft());
        DataSourceExecuteUtil.execute(getSourceDataSource(), getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        log.info("init data end: {}", LocalDateTime.now());
        startMigration(getSourceTableOrderName(), getTargetTableOrderName());
        startMigration("t_order_item", "t_order_item");
        String orderJobId = getJobIdByTableName("ds_0." + getSourceTableOrderName());
        waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", orderJobId));
        startIncrementTask(new MySQLIncrementTask(getSourceDataSource(), getSourceTableOrderName(), new SnowflakeKeyGenerateAlgorithm(), 30));
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
        proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        assertGreaterThanOrderTableInitRows(PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT, "");
        log.info("{} E2E IT finished, database type={}, docker image={}", this.getClass().getName(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
    }
    
    private void assertMigrationSuccessById(final String jobId, final String algorithmType) throws SQLException, InterruptedException {
        List<Map<String, Object>> jobStatus = waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        for (Map<String, Object> each : jobStatus) {
            assertTrue(Integer.parseInt(each.get("processed_records_count").toString()) > 0);
            assertThat(Integer.parseInt(each.get("inventory_finished_percentage").toString()), is(100));
        }
        assertCheckMigrationSuccess(jobId, algorithmType);
    }
}
