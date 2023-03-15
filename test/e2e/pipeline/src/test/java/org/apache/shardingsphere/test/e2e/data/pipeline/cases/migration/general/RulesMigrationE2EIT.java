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

import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * E2E IT for different types of rules, includes:
 * 1) no any rule.
 * 2) only encrypt rule.
 */
public final class RulesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertNoRuleMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            assertMigrationSuccess(containerComposer, null);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertOnlyEncryptRuleMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
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
        String jobId = listJobId(containerComposer).get(0);
        containerComposer.waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
        commitMigrationByJobId(containerComposer, jobId);
        containerComposer.proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertThat(containerComposer.getTargetTableRecordsCount(SOURCE_TABLE_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT));
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(new MySQLDatabaseType());
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            List<String> versions = PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType());
            return Stream.of(Arguments.of(new PipelineTestParameter(new MySQLDatabaseType(), versions.get(0), "env/scenario/primary_key/text_primary_key/mysql.xml")));
        }
    }
}
