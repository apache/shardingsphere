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
 * E2E IT for time types of MySQL, includes.
 * 1) datetime,timestamp,date default null
 */
@PipelineE2ESettings(fetchSingle = true, database = @PipelineE2ESettings.PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"))
class MySQLTimeTypesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertIllegalTimeTypesValueMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            String sql = "CREATE TABLE `time_e2e` ( `id` int NOT NULL, `t_timestamp` timestamp NULL DEFAULT NULL, `t_datetime` datetime DEFAULT NULL, `t_date` date DEFAULT NULL, "
                    + "`t_year` year DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB;";
            containerComposer.sourceExecuteWithLog(sql);
            insertOneRecordWithZeroValue(containerComposer, 1);
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            startMigration(containerComposer, "time_e2e", "time_e2e");
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            String jobId = distSQLFacade.listJobIds().get(0);
            distSQLFacade.waitJobPrepareSuccess(jobId);
            insertOneRecordWithZeroValue(containerComposer, 2);
            distSQLFacade.waitIncrementTaskFinished(jobId);
            distSQLFacade.loadAllSingleTables();
            assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
        }
    }
    
    private void insertOneRecordWithZeroValue(final PipelineContainerComposer containerComposer, final int id) throws SQLException {
        try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `time_e2e`(id, t_timestamp, t_datetime, t_date, t_year) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setObject(1, id);
            preparedStatement.setObject(2, "0000-00-00 00:00:00");
            preparedStatement.setObject(3, "0000-00-00 00:00:00");
            preparedStatement.setObject(4, "0000-00-00");
            preparedStatement.setObject(5, "0000");
            preparedStatement.execute();
        }
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
