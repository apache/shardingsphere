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
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * E2E IT for zero datetime with fractional seconds of MySQL.
 */
@PipelineE2ESettings(fetchSingle = true, database = @PipelineE2ESettings.PipelineE2EDatabaseSettings(type = "MySQL"))
class MySQLFractionalDatetimeMigrationE2EIT extends AbstractMigrationE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertZeroDatetimeWithFractionMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            String sql = "CREATE TABLE `fractional_datetime_e2e` ( `id` int NOT NULL, `t_datetime_4` datetime(4) NOT NULL DEFAULT '0000-00-00 00:00:00.0000', "
                    + "PRIMARY KEY (`id`)) ENGINE=InnoDB;";
            containerComposer.sourceExecuteWithLog(sql);
            insertOneRecordWithDefaultZeroDatetime(containerComposer, 1);
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            startMigration(containerComposer, "fractional_datetime_e2e", "fractional_datetime_e2e");
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            String jobId = distSQLFacade.listJobIds().get(0);
            distSQLFacade.waitJobPreparingStageFinished(jobId);
            insertOneRecordWithDefaultZeroDatetime(containerComposer, 2);
            distSQLFacade.waitJobIncrementalStageFinished(jobId);
            distSQLFacade.loadAllSingleTables();
            distSQLFacade.startCheckAndVerify(jobId, "DATA_MATCH");
        }
    }
    
    private void insertOneRecordWithDefaultZeroDatetime(final PipelineContainerComposer containerComposer, final int id) throws SQLException {
        try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `fractional_datetime_e2e`(id) VALUES (?)");
            preparedStatement.setObject(1, id);
            preparedStatement.execute();
        }
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
