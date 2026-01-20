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

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.database.connector.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.algorithm.keygen.uuid.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * E2E IT for different types of indexes, includes:
 * 1) no unique key.
 * 2) special type single column unique key, e.g. VARBINARY.
 * 3) multiple columns primary key, first column type is VARCHAR.
 * 4) multiple columns unique key, first column type is BIGINT.
 */
@PipelineE2ESettings(fetchSingle = true, database = {
        @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"),
        @PipelineE2EDatabaseSettings(type = "PostgreSQL", scenarioFiles = "env/common/none.xml")})
class IndexesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String ORDER_TABLE_SHARDING_RULE_FORMAT = "CREATE SHARDING TABLE RULE t_order(\n"
            + "STORAGE_UNITS(ds_2,ds_3,ds_4),\n"
            + "SHARDING_COLUMN=%s,\n"
            + "TYPE(NAME=\"hash_mod\",PROPERTIES(\"sharding-count\"=\"6\"))\n"
            + ");";
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertNoUniqueKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
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
            // TODO PostgreSQL update delete events not support if table without unique keys at increment task.
            final Consumer<DataSource> incrementalTaskFn = dataSource -> {
                if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                    doCreateUpdateDelete(containerComposer, "a1");
                }
                Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
                insertOneOrder(containerComposer, orderId);
                containerComposer.assertOrderRecordExist(dataSource, "t_order", orderId);
            };
            assertMigrationSuccess(containerComposer, sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, incrementalTaskFn);
        }
    }
    
    @SneakyThrows
    private void doCreateUpdateDelete(final PipelineContainerComposer containerComposer, final Object orderId) {
        String updatedStatus = "updated" + System.currentTimeMillis();
        insertOneOrder(containerComposer, orderId);
        updateOneOrder(containerComposer, orderId, updatedStatus);
        deleteOneOrder(containerComposer, orderId, updatedStatus);
    }
    
    private void insertOneOrder(final PipelineContainerComposer containerComposer, final Object uniqueKey) {
        try (
                Connection connection = containerComposer.getSourceDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (order_id,user_id,status) VALUES (?,?,?)")) {
            preparedStatement.setObject(1, uniqueKey);
            preparedStatement.setObject(2, 1);
            preparedStatement.setObject(3, "OK");
            int actualCount = preparedStatement.executeUpdate();
            assertThat(actualCount, is(1));
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private void updateOneOrder(final PipelineContainerComposer containerComposer, final Object uniqueKey, final String updatedStatus) {
        try (
                Connection connection = containerComposer.getSourceDataSource().getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("UPDATE t_order SET status=? WHERE order_id = ? AND user_id = ? AND status = ?")) {
            preparedStatement.setObject(1, updatedStatus);
            preparedStatement.setObject(2, uniqueKey);
            preparedStatement.setObject(3, 1);
            preparedStatement.setObject(4, "OK");
            int actualCount = preparedStatement.executeUpdate();
            assertThat(actualCount, is(1));
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private void deleteOneOrder(final PipelineContainerComposer containerComposer, final Object uniqueKey, final String updatedStatus) {
        try (
                Connection connection = containerComposer.getSourceDataSource().getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("DELETE FROM t_order WHERE order_id = ? AND user_id = ? AND status = ?")) {
            preparedStatement.setObject(1, uniqueKey);
            preparedStatement.setObject(2, 1);
            preparedStatement.setObject(3, updatedStatus);
            int actualCount = preparedStatement.executeUpdate();
            assertThat(actualCount, is(1));
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMultiPrimaryKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            String sql;
            String consistencyCheckAlgorithmType;
            if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                consistencyCheckAlgorithmType = "CRC32_MATCH";
            } else {
                return;
            }
            KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
            Object uniqueKey = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
            assertMigrationSuccess(containerComposer, sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, dataSource -> {
                insertOneOrder(containerComposer, uniqueKey);
                doCreateUpdateDelete(containerComposer, keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next());
                containerComposer.assertOrderRecordExist(dataSource, "t_order", uniqueKey);
            });
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertMultiUniqueKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            String sql;
            String consistencyCheckAlgorithmType;
            if (containerComposer.getDatabaseType() instanceof MySQLDatabaseType) {
                sql = "CREATE TABLE `%s` (`order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), UNIQUE KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                consistencyCheckAlgorithmType = "DATA_MATCH";
            } else {
                return;
            }
            KeyGenerateAlgorithm keyGenerateAlgorithm = new AutoIncrementKeyGenerateAlgorithm();
            Object uniqueKey = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
            assertMigrationSuccess(containerComposer, sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, dataSource -> {
                insertOneOrder(containerComposer, uniqueKey);
                doCreateUpdateDelete(containerComposer, keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next());
                containerComposer.assertOrderRecordExist(dataSource, "t_order", uniqueKey);
            });
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertSpecialTypeSingleColumnUniqueKeyMigrationSuccess(final PipelineTestParameter testParam) throws Exception {
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
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
            assertMigrationSuccess(containerComposer, sql, "order_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, dataSource -> {
                insertOneOrder(containerComposer, uniqueKey);
                // TODO Select by byte[] from proxy doesn't work, so unhex function is used for now
                containerComposer.assertOrderRecordExist(dataSource, String.format("SELECT 1 FROM t_order WHERE order_id=UNHEX('%s')", Hex.encodeHexString(uniqueKey)));
            });
        }
    }
    
    private void assertMigrationSuccess(final PipelineContainerComposer containerComposer, final String sqlPattern, final String shardingColumn, final KeyGenerateAlgorithm keyGenerateAlgorithm,
                                        final String consistencyCheckAlgorithmType, final Consumer<DataSource> incrementalTaskFn) throws Exception {
        containerComposer.sourceExecuteWithLog(String.format(sqlPattern, SOURCE_TABLE_NAME));
        try (Connection connection = containerComposer.getSourceDataSource().getConnection()) {
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, SOURCE_TABLE_NAME, PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        }
        PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new MigrationJobType());
        distSQLFacade.alterPipelineRule();
        addMigrationSourceResource(containerComposer);
        addMigrationTargetResource(containerComposer);
        containerComposer.proxyExecuteWithLog(String.format(ORDER_TABLE_SHARDING_RULE_FORMAT, shardingColumn), 2);
        startMigration(containerComposer, SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
        String jobId = distSQLFacade.listJobIds().get(0);
        distSQLFacade.waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
        incrementalTaskFn.accept(jdbcDataSource);
        distSQLFacade.waitIncrementTaskFinished(jobId);
        if (null != consistencyCheckAlgorithmType) {
            assertCheckMigrationSuccess(containerComposer, jobId, consistencyCheckAlgorithmType);
        }
        distSQLFacade.commit(jobId);
        assertThat(containerComposer.getTargetTableRecordsCount(jdbcDataSource, SOURCE_TABLE_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1));
        assertTrue(distSQLFacade.listJobIds().isEmpty());
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
