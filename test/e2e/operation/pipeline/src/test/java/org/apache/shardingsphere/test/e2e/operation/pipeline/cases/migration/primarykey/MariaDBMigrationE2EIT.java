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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.primarykey;

import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.algorithm.keygen.uuid.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO Use MariaDB docker image
@Disabled
@PipelineE2ESettings(fetchSingle = true, database = @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"))
class MariaDBMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            String sqlPattern = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            containerComposer.sourceExecuteWithLog(String.format(sqlPattern, SOURCE_TABLE_NAME));
            try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
                KeyGenerateAlgorithm generateAlgorithm = new UUIDKeyGenerateAlgorithm();
                PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, generateAlgorithm, SOURCE_TABLE_NAME, PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            }
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            distSQLFacade.alterPipelineRule();
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetOrderTableRule(containerComposer);
            startMigration(containerComposer, SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
            String jobId = distSQLFacade.listJobIds().get(0);
            distSQLFacade.waitJobPreparingStageFinished(jobId);
            containerComposer.sourceExecuteWithLog("INSERT INTO t_order (order_id, user_id, status) VALUES ('a1', 1, 'OK')");
            DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            containerComposer.assertOrderRecordExist(jdbcDataSource, "t_order", "a1");
            distSQLFacade.waitJobIncrementalStageFinished(jobId);
            distSQLFacade.startCheckAndVerify(jobId, "CRC32_MATCH");
            distSQLFacade.commit(jobId);
            assertThat(containerComposer.getTargetTableRecordsCount(jdbcDataSource, SOURCE_TABLE_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1));
            assertTrue(distSQLFacade.listJobIds().isEmpty());
        }
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
