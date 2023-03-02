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
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MariaDBMigrationE2EIT extends AbstractMigrationE2EIT {
    
    public MariaDBMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order";
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertMigrationSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        initEnvironment(getDatabaseType(), new MigrationJobType());
        String sqlPattern = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        sourceExecuteWithLog(String.format(sqlPattern, getSourceTableOrderName()));
        try (Connection connection = getSourceDataSource().getConnection()) {
            KeyGenerateAlgorithm generateAlgorithm = new UUIDKeyGenerateAlgorithm();
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, generateAlgorithm, getSourceTableOrderName(), PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT);
        }
        addMigrationProcessConfig();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        startMigration(getSourceTableOrderName(), getTargetTableOrderName());
        String jobId = listJobId().get(0);
        waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        sourceExecuteWithLog("INSERT INTO t_order (order_id, user_id, status) VALUES ('a1', 1, 'OK')");
        assertProxyOrderRecordExist("t_order", "a1");
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "CRC32_MATCH");
        commitMigrationByJobId(jobId);
        proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertThat(getTargetTableRecordsCount(getSourceTableOrderName()), is(PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT + 1));
        assertTrue(listJobId().isEmpty());
    }
    
    private static boolean isEnabled() {
        return PipelineBaseE2EIT.ENV.getItEnvType() != PipelineEnvTypeEnum.NONE && !PipelineBaseE2EIT.ENV.listStorageContainerImages(new MySQLDatabaseType()).isEmpty();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<Arguments> result = new LinkedList<>();
            List<String> versions = PipelineBaseE2EIT.ENV.listStorageContainerImages(new MySQLDatabaseType());
            // TODO use MariaDBDatabaseType
            result.add(Arguments.of(new PipelineTestParameter(new MySQLDatabaseType(), versions.get(0), "env/common/none.xml")));
            return result.stream();
        }
    }
}
