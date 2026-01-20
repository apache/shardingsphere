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
import org.apache.shardingsphere.database.connector.mysql.type.MySQLDatabaseType;
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
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PipelineE2ESettings(fetchSingle = true, database = {
        @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/scenario/primary_key/text_primary_key/mysql.xml"),
        @PipelineE2EDatabaseSettings(type = "PostgreSQL", scenarioFiles = "env/scenario/primary_key/text_primary_key/postgresql.xml"),
        @PipelineE2EDatabaseSettings(type = "openGauss", scenarioFiles = "env/scenario/primary_key/text_primary_key/postgresql.xml")})
class TextPrimaryKeyMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertTextPrimaryMigrationSuccess(final PipelineTestParameter testParam) throws SQLException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            containerComposer.createSourceOrderTable(getSourceTableName(containerComposer));
            try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
                UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
                PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, getSourceTableName(containerComposer), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            }
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
            distSQLFacade.alterPipelineRule();
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetOrderTableRule(containerComposer);
            startMigration(containerComposer, getSourceTableName(containerComposer), TARGET_TABLE_NAME);
            String jobId = distSQLFacade.listJobIds().get(0);
            containerComposer.sourceExecuteWithLog(
                    String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')", getSourceTableName(containerComposer), "1000000000", 1, "afterStop"));
            distSQLFacade.waitIncrementTaskFinished(jobId);
            startCheckAndVerify(containerComposer, jobId, "DATA_MATCH");
            distSQLFacade.commit(jobId);
            assertTrue(distSQLFacade.listJobIds().isEmpty());
        }
    }
    
    private String getSourceTableName(final PipelineContainerComposer containerComposer) {
        return containerComposer.getDatabaseType() instanceof MySQLDatabaseType ? "T_ORDER" : "t_order";
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
