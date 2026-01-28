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

import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(fetchSingle = true, database = @PipelineE2ESettings.PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"))
class RollbackMigrationE2EIT extends AbstractMigrationE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertRollbackSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            String sql = "CREATE TABLE t_order (order_id BIGINT PRIMARY KEY, user_id INT, status VARCHAR(50))";
            containerComposer.sourceExecuteWithLog(sql);
            try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
                PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, new AutoIncrementKeyGenerateAlgorithm(), "t_order", 10);
            }
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            startMigration(containerComposer, "t_order", "t_order");
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            String jobId = distSQLFacade.listJobIds().get(0);
            distSQLFacade.rollback(jobId);
            assertTrue(distSQLFacade.listJobIds().isEmpty());
        }
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
