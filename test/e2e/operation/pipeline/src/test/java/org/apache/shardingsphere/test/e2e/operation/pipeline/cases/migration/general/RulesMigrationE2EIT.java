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
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * E2E IT for different types of rules, includes:
 * 1) no any rule.
 * 2) only encrypt rule.
 */
@PipelineE2ESettings(fetchSingle = true, database = {
        @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/scenario/primary_key/text_primary_key/mysql.xml"),
        @PipelineE2EDatabaseSettings(type = "MariaDB", scenarioFiles = "env/scenario/primary_key/text_primary_key/mysql.xml")
})
class RulesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertNoRuleMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            assertMigrationSuccess(containerComposer, null);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertOnlyEncryptRuleMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            assertMigrationSuccess(containerComposer, () -> {
                createTargetOrderTableEncryptRule(containerComposer);
                return null;
            });
        }
    }
    
    private void assertMigrationSuccess(final PipelineContainerComposer containerComposer, final Callable<Void> addRuleFn) throws Exception {
        containerComposer.createSourceOrderTable(SOURCE_TABLE_NAME);
        try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, new UUIDKeyGenerateAlgorithm(), SOURCE_TABLE_NAME, PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        }
        addMigrationSourceResource(containerComposer);
        addMigrationTargetResource(containerComposer);
        if (null != addRuleFn) {
            addRuleFn.call();
        }
        startMigration(containerComposer, SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
        PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
        String jobId = distSQLFacade.listJobIds().get(0);
        containerComposer.waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        distSQLFacade.loadAllSingleTables();
        assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
        distSQLFacade.commit(jobId);
        assertThat(containerComposer.getTargetTableRecordsCount(containerComposer.getProxyDataSource(), SOURCE_TABLE_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT));
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
