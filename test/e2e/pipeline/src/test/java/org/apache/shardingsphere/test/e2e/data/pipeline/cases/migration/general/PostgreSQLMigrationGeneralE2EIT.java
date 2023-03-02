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
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.PostgreSQLIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public final class PostgreSQLMigrationGeneralE2EIT extends AbstractMigrationE2EIT {
    
    public PostgreSQLMigrationGeneralE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order_copy";
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        log.info("assertMigrationSuccess testParam:{}", testParam);
        initEnvironment(testParam.getDatabaseType(), new MigrationJobType());
        addMigrationProcessConfig();
        createSourceSchema(PipelineBaseE2EIT.SCHEMA_NAME);
        createSourceOrderTable();
        createSourceOrderItemTable();
        createSourceTableIndexList(PipelineBaseE2EIT.SCHEMA_NAME);
        createSourceCommentOnList(PipelineBaseE2EIT.SCHEMA_NAME);
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        createTargetOrderItemTableRule();
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(testParam.getDatabaseType(), PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(getSourceDataSource(), getExtraSQLCommand().getFullInsertOrder(getSourceTableOrderName()), dataPair.getLeft());
        DataSourceExecuteUtil.execute(getSourceDataSource(), getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        log.info("init data end: {}", LocalDateTime.now());
        checkOrderMigration();
        checkOrderItemMigration();
        for (String each : listJobId()) {
            commitMigrationByJobId(each);
        }
        List<String> lastJobIds = listJobId();
        assertTrue(lastJobIds.isEmpty());
        proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        assertGreaterThanOrderTableInitRows(PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT + 1, PipelineBaseE2EIT.SCHEMA_NAME);
        log.info("{} E2E IT finished, database type={}, docker image={}", this.getClass().getName(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
    }
    
    private void checkOrderMigration() throws SQLException, InterruptedException {
        startMigrationWithSchema(getSourceTableOrderName(), "t_order");
        startIncrementTask(new PostgreSQLIncrementTask(getSourceDataSource(), PipelineBaseE2EIT.SCHEMA_NAME, getSourceTableOrderName(), 20));
        String jobId = getJobIdByTableName("ds_0.test." + getSourceTableOrderName());
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        stopMigrationByJobId(jobId);
        long recordId = new SnowflakeKeyGenerateAlgorithm().generateKey();
        sourceExecuteWithLog(
                String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')", String.join(".", PipelineBaseE2EIT.SCHEMA_NAME, getSourceTableOrderName()), recordId, 1, "afterStop"));
        startMigrationByJobId(jobId);
        // must refresh firstly, otherwise proxy can't get schema and table info
        proxyExecuteWithLog("REFRESH TABLE METADATA;", 2);
        assertProxyOrderRecordExist(String.join(".", PipelineBaseE2EIT.SCHEMA_NAME, getTargetTableOrderName()), recordId);
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
    }
    
    private void checkOrderItemMigration() throws SQLException, InterruptedException {
        startMigrationWithSchema("t_order_item", "t_order_item");
        String jobId = getJobIdByTableName("ds_0.test.t_order_item");
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
    }
    
    private static boolean isEnabled() {
        return PipelineBaseE2EIT.ENV.getItEnvType() != PipelineEnvTypeEnum.NONE;
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<Arguments> result = new LinkedList<>();
            for (String each : PipelineBaseE2EIT.ENV.listStorageContainerImages(new PostgreSQLDatabaseType())) {
                result.add(Arguments.of(new PipelineTestParameter(new PostgreSQLDatabaseType(), each, "env/scenario/general/postgresql.xml")));
            }
            for (String each : PipelineBaseE2EIT.ENV.listStorageContainerImages(new OpenGaussDatabaseType())) {
                result.add(Arguments.of(new PipelineTestParameter(new OpenGaussDatabaseType(), each, "env/scenario/general/postgresql.xml")));
            }
            return result.stream();
        }
    }
}
