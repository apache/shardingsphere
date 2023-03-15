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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.primarykey;

import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtil;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextPrimaryKeyMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertTextPrimaryMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            containerComposer.createSourceOrderTable(getSourceTableName(containerComposer));
            try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
                UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
                PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, getSourceTableName(containerComposer), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            }
            addMigrationProcessConfig(containerComposer);
            addMigrationSourceResource(containerComposer);
            addMigrationTargetResource(containerComposer);
            createTargetOrderTableRule(containerComposer);
            startMigration(containerComposer, getSourceTableName(containerComposer), TARGET_TABLE_NAME);
            String jobId = listJobId(containerComposer).get(0);
            containerComposer.sourceExecuteWithLog(
                    String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')", getSourceTableName(containerComposer), "1000000000", 1, "afterStop"));
            containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
            assertCheckMigrationSuccess(containerComposer, jobId, "DATA_MATCH");
            commitMigrationByJobId(containerComposer, jobId);
            List<String> lastJobIds = listJobId(containerComposer);
            assertTrue(lastJobIds.isEmpty());
        }
    }
    
    private String getSourceTableName(final PipelineContainerComposer containerComposer) {
        return DatabaseTypeUtil.isMySQL(containerComposer.getDatabaseType()) ? "T_ORDER" : "t_order";
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(new MySQLDatabaseType(), new PostgreSQLDatabaseType(), new OpenGaussDatabaseType());
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<Arguments> result = new LinkedList<>();
            for (String version : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType())) {
                result.add(Arguments.of(new PipelineTestParameter(new MySQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/mysql.xml")));
            }
            for (String version : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new PostgreSQLDatabaseType())) {
                result.add(Arguments.of(new PipelineTestParameter(new PostgreSQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml")));
            }
            for (String version : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new OpenGaussDatabaseType())) {
                result.add(Arguments.of(new PipelineTestParameter(new OpenGaussDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml")));
            }
            return result.stream();
        }
    }
}
