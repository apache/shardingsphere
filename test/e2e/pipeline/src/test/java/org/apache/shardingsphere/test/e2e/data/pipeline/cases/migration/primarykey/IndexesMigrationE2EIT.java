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

import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E2E IT for different types of indexes, includes:
 * 1) no unique key.
 * 2) special type single column unique key, e.g. VARBINARY.
 * 3) multiple columns primary key, first column type is VARCHAR.
 * 4) multiple columns unique key, first column type is BIGINT.
 */
public final class IndexesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String ORDER_TABLE_SHARDING_RULE_FORMAT = "CREATE SHARDING TABLE RULE t_order(\n"
            + "STORAGE_UNITS(ds_2,ds_3,ds_4),\n"
            + "SHARDING_COLUMN=%s,\n"
            + "TYPE(NAME=\"hash_mod\",PROPERTIES(\"sharding-count\"=\"6\"))\n"
            + ");";
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertNoUniqueKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            String sql;
            String consistencyCheckAlgorithmType;
            if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                // DATA_MATCH doesn't supported, could not order by records
                consistencyCheckAlgorithmType = "CRC32_MATCH";
            } else if (containerComposer.getDatabaseType() instanceof PostgreSQLDatabaseType) {
                sql = "CREATE TABLE %s (order_id varchar(255) NOT NULL,user_id int NOT NULL,status varchar(255) NULL)";
                consistencyCheckAlgorithmType = null;
            } else {
                return;
            }
            KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
            Object uniqueKey = keyGenerateAlgorithm.generateKey();
            assertMigrationSuccess(containerComposer, sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
                insertOneOrder(containerComposer, uniqueKey);
                containerComposer.assertProxyOrderRecordExist("t_order", uniqueKey);
                return null;
            });
        }
    }
    
    private void insertOneOrder(final PipelineContainerComposer containerComposer, final Object uniqueKey) throws SQLException {
        try (PreparedStatement preparedStatement = containerComposer.getSourceDataSource().getConnection().prepareStatement("INSERT INTO t_order (order_id,user_id,status) VALUES (?,?,?)")) {
            preparedStatement.setObject(1, uniqueKey);
            preparedStatement.setObject(2, 1);
            preparedStatement.setObject(3, "OK");
            int actualCount = preparedStatement.executeUpdate();
            assertThat(actualCount, is(1));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertMultiPrimaryKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            String sql;
            String consistencyCheckAlgorithmType;
            if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                consistencyCheckAlgorithmType = "CRC32_MATCH";
            } else {
                return;
            }
            KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
            Object uniqueKey = keyGenerateAlgorithm.generateKey();
            assertMigrationSuccess(containerComposer, sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
                insertOneOrder(containerComposer, uniqueKey);
                containerComposer.assertProxyOrderRecordExist("t_order", uniqueKey);
                return null;
            });
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertMultiUniqueKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            String sql;
            String consistencyCheckAlgorithmType;
            if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                sql = "CREATE TABLE `%s` (`order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), UNIQUE KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                consistencyCheckAlgorithmType = "DATA_MATCH";
            } else {
                return;
            }
            KeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
            Object uniqueKey = keyGenerateAlgorithm.generateKey();
            assertMigrationSuccess(containerComposer, sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
                insertOneOrder(containerComposer, uniqueKey);
                containerComposer.assertProxyOrderRecordExist("t_order", uniqueKey);
                return null;
            });
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertSpecialTypeSingleColumnUniqueKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new MigrationJobType())) {
            String sql;
            String consistencyCheckAlgorithmType;
            if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                sql = "CREATE TABLE `%s` (`order_id` VARBINARY(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                // DATA_MATCH doesn't supported: Order by value must implements Comparable
                consistencyCheckAlgorithmType = "CRC32_MATCH";
            } else {
                return;
            }
            KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
            // TODO Insert binary string in VARBINARY column. But KeyGenerateAlgorithm.generateKey() require returning Comparable, and byte[] is not Comparable
            byte[] uniqueKey = new byte[]{-1, 0, 1};
            assertMigrationSuccess(containerComposer, sql, "order_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
                insertOneOrder(containerComposer, uniqueKey);
                // TODO Select by byte[] from proxy doesn't work, so unhex function is used for now
                containerComposer.assertProxyOrderRecordExist(String.format("SELECT 1 FROM t_order WHERE order_id=UNHEX('%s')", Hex.encodeHexString(uniqueKey)));
                return null;
            });
        }
    }
    
    private void assertMigrationSuccess(final PipelineContainerComposer containerComposer, final String sqlPattern, final String shardingColumn, final KeyGenerateAlgorithm keyGenerateAlgorithm,
                                        final String consistencyCheckAlgorithmType, final Callable<Void> incrementalTaskFn) throws Exception {
        containerComposer.sourceExecuteWithLog(String.format(sqlPattern, SOURCE_TABLE_NAME));
        try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, SOURCE_TABLE_NAME, PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        }
        addMigrationProcessConfig(containerComposer);
        addMigrationSourceResource(containerComposer);
        addMigrationTargetResource(containerComposer);
        containerComposer.proxyExecuteWithLog(String.format(ORDER_TABLE_SHARDING_RULE_FORMAT, shardingColumn), 2);
        startMigration(containerComposer, SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
        String jobId = listJobId(containerComposer).get(0);
        containerComposer.waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        incrementalTaskFn.call();
        containerComposer.waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        if (null != consistencyCheckAlgorithmType) {
            assertCheckMigrationSuccess(containerComposer, jobId, consistencyCheckAlgorithmType);
        }
        commitMigrationByJobId(containerComposer, jobId);
        containerComposer.proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertThat(containerComposer.getTargetTableRecordsCount(SOURCE_TABLE_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1));
        List<String> lastJobIds = listJobId(containerComposer);
        assertTrue(lastJobIds.isEmpty());
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(new MySQLDatabaseType(), new PostgreSQLDatabaseType());
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<Arguments> result = new LinkedList<>();
            List<String> mysqlVersion = PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType());
            if (!mysqlVersion.isEmpty()) {
                result.add(Arguments.of(new PipelineTestParameter(new MySQLDatabaseType(), mysqlVersion.get(0), "env/common/none.xml")));
            }
            List<String> postgresqlVersion = PipelineE2EEnvironment.getInstance().listStorageContainerImages(new PostgreSQLDatabaseType());
            if (!postgresqlVersion.isEmpty()) {
                result.add(Arguments.of(new PipelineTestParameter(new PostgreSQLDatabaseType(), postgresqlVersion.get(0), "env/common/none.xml")));
            }
            return result.stream();
        }
    }
}
