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
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(Parameterized.class)
@Slf4j
public final class PostgreSQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_ORDER_NAME = "t_order_copy";
    
    public PostgreSQLMigrationGeneralE2EIT(final PipelineTestParameter testParam) {
        super(testParam, new MigrationJobType());
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new PostgreSQLDatabaseType())) {
            result.add(new PipelineTestParameter(new PostgreSQLDatabaseType(), each, "env/scenario/general/postgresql.xml"));
        }
        for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new OpenGaussDatabaseType())) {
            result.add(new PipelineTestParameter(new OpenGaussDatabaseType(), each, "env/scenario/general/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertMigrationSuccess() throws SQLException, InterruptedException {
        addMigrationProcessConfig();
        createSourceSchema(PipelineContainerComposer.SCHEMA_NAME);
        getContainerComposer().createSourceOrderTable(SOURCE_TABLE_ORDER_NAME);
        getContainerComposer().createSourceOrderItemTable();
        getContainerComposer().createSourceTableIndexList(PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_ORDER_NAME);
        getContainerComposer().createSourceCommentOnList(PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_ORDER_NAME);
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        createTargetOrderItemTableRule();
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(getContainerComposer().getDatabaseType(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(getContainerComposer().getSourceDataSource(), getContainerComposer().getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_ORDER_NAME), dataPair.getLeft());
        DataSourceExecuteUtil.execute(getContainerComposer().getSourceDataSource(), getContainerComposer().getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        log.info("init data end: {}", LocalDateTime.now());
        startMigrationWithSchema(SOURCE_TABLE_ORDER_NAME, "t_order");
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> listJobId().size() > 0);
        String jobId = getJobIdByTableName("ds_0.test." + SOURCE_TABLE_ORDER_NAME);
        getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        getContainerComposer().startIncrementTask(new E2EIncrementalTask(
                getContainerComposer().getSourceDataSource(), String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_ORDER_NAME),
                new SnowflakeKeyGenerateAlgorithm(), getContainerComposer().getDatabaseType(), 20));
        checkOrderMigration(jobId);
        checkOrderItemMigration();
        for (String each : listJobId()) {
            commitMigrationByJobId(each);
        }
        List<String> lastJobIds = listJobId();
        assertTrue(lastJobIds.isEmpty());
        getContainerComposer().proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        getContainerComposer().assertGreaterThanOrderTableInitRows(PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1, PipelineContainerComposer.SCHEMA_NAME);
    }
    
    private void checkOrderMigration(final String jobId) throws SQLException, InterruptedException {
        getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        stopMigrationByJobId(jobId);
        long recordId = new SnowflakeKeyGenerateAlgorithm().generateKey();
        getContainerComposer().sourceExecuteWithLog(String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')",
                String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_ORDER_NAME), recordId, 1, "afterStop"));
        startMigrationByJobId(jobId);
        // must refresh firstly, otherwise proxy can't get schema and table info
        getContainerComposer().proxyExecuteWithLog("REFRESH TABLE METADATA;", 2);
        getContainerComposer().assertProxyOrderRecordExist(String.join(".", PipelineContainerComposer.SCHEMA_NAME, getContainerComposer().getTargetTableOrderName()), recordId);
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
    }
    
    private void checkOrderItemMigration() throws SQLException, InterruptedException {
        startMigrationWithSchema("t_order_item", "t_order_item");
        String jobId = getJobIdByTableName("ds_0.test.t_order_item");
        getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
    }
}
