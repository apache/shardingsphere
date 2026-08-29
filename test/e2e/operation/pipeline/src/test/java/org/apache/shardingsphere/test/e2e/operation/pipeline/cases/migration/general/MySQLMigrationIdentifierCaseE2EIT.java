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

import com.google.common.collect.ImmutableMap;
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
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(database = @PipelineE2EDatabaseSettings(type = "MySQL"))
class MySQLMigrationIdentifierCaseE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order_copy";
    
    private static final String TARGET_TABLE_NAME = "T_ORDER";
    
    private static final long INVENTORY_ORDER_ID = 1L;
    
    private static final long INCREMENTAL_ORDER_ID = 2L;
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationToUpperCaseTargetTable(final PipelineTestParameter testParam) throws SQLException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            assertCaseSensitiveTableNames(containerComposer);
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            distSQLFacade.alterPipelineRule();
            containerComposer.sourceExecuteWithLog(String.format(
                    "CREATE TABLE %s (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(32) NOT NULL)", SOURCE_TABLE_NAME));
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetTableRule(containerComposer);
            insertSourceRecord(containerComposer, INVENTORY_ORDER_ID);
            containerComposer.proxyExecuteWithLog(String.format("MIGRATE TABLE ds_0.%s INTO sharding_DB.%s", SOURCE_TABLE_NAME, TARGET_TABLE_NAME), 5);
            Awaitility.waitAtMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !distSQLFacade.listJobIds().isEmpty());
            String jobId = distSQLFacade.getJobIdByTableName("ds_0." + SOURCE_TABLE_NAME);
            distSQLFacade.waitJobPreparingStageFinished(jobId);
            distSQLFacade.waitJobIncrementalStageStarted(jobId);
            insertSourceRecord(containerComposer, INCREMENTAL_ORDER_ID);
            assertFalse(distSQLFacade.waitJobIncrementalStageFinished(jobId).isEmpty(), "Migration incremental stage did not become idle");
            DataSource targetDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            containerComposer.assertRecordExists(targetDataSource, TARGET_TABLE_NAME, INVENTORY_ORDER_ID);
            containerComposer.assertRecordExists(targetDataSource, TARGET_TABLE_NAME, INCREMENTAL_ORDER_ID);
            distSQLFacade.startCheck(jobId, "DATA_MATCH", ImmutableMap.of("chunk-size", "300", "streaming-range-type", "SMALL"));
            distSQLFacade.verifyCheck(jobId);
            distSQLFacade.commit(jobId);
            assertTrue(distSQLFacade.listJobIds().isEmpty());
        }
    }
    
    private void assertCaseSensitiveTableNames(final PipelineContainerComposer containerComposer) throws SQLException {
        try (
                Connection connection = containerComposer.getSourceDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT @@lower_case_table_names")) {
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(0));
        }
    }
    
    private void createTargetTableRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("CREATE SHARDING TABLE RULE %s(STORAGE_UNITS(ds_2,ds_3,ds_4),SHARDING_COLUMN=user_id,"
                + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='6')))", TARGET_TABLE_NAME), 0);
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS)
                .until(() -> !containerComposer.queryForListWithLog("SHOW SHARDING TABLE RULE " + TARGET_TABLE_NAME).isEmpty());
    }
    
    private void insertSourceRecord(final PipelineContainerComposer containerComposer, final long orderId) throws SQLException {
        containerComposer.sourceExecuteWithLog(String.format("INSERT INTO %s VALUES (%d, 1, 'OK')", SOURCE_TABLE_NAME, orderId));
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
