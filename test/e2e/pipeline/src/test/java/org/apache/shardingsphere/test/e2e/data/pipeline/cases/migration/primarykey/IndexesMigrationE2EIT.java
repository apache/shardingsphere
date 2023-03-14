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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

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
@RunWith(Parameterized.class)
@Slf4j
public final class IndexesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private static final String ORDER_TABLE_SHARDING_RULE_FORMAT = "CREATE SHARDING TABLE RULE t_order(\n"
            + "STORAGE_UNITS(ds_2,ds_3,ds_4),\n"
            + "SHARDING_COLUMN=%s,\n"
            + "TYPE(NAME=\"hash_mod\",PROPERTIES(\"sharding-count\"=\"6\"))\n"
            + ");";
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    private static final String TARGET_TABLE_NAME = "t_order";
    
    public IndexesMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam, new MigrationJobType());
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        List<String> mysqlVersion = PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType());
        if (!mysqlVersion.isEmpty()) {
            result.add(new PipelineTestParameter(new MySQLDatabaseType(), mysqlVersion.get(0), "env/common/none.xml"));
        }
        List<String> postgresqlVersion = PipelineE2EEnvironment.getInstance().listStorageContainerImages(new PostgreSQLDatabaseType());
        if (!postgresqlVersion.isEmpty()) {
            result.add(new PipelineTestParameter(new PostgreSQLDatabaseType(), postgresqlVersion.get(0), "env/common/none.xml"));
        }
        return result;
    }
    
    @Test
    public void assertNoUniqueKeyMigrationSuccess() throws Exception {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getContainerComposer().getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            // DATA_MATCH doesn't supported, could not order by records
            consistencyCheckAlgorithmType = "CRC32_MATCH";
        } else if (getContainerComposer().getDatabaseType() instanceof PostgreSQLDatabaseType) {
            sql = "CREATE TABLE %s (order_id varchar(255) NOT NULL,user_id int NOT NULL,status varchar(255) NULL)";
            consistencyCheckAlgorithmType = null;
        } else {
            return;
        }
        KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        Object uniqueKey = keyGenerateAlgorithm.generateKey();
        assertMigrationSuccess(sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
            insertOneOrder(uniqueKey);
            getContainerComposer().assertProxyOrderRecordExist("t_order", uniqueKey);
            return null;
        });
    }
    
    private void insertOneOrder(final Object uniqueKey) throws SQLException {
        try (PreparedStatement preparedStatement = getContainerComposer().getSourceDataSource().getConnection().prepareStatement("INSERT INTO t_order (order_id,user_id,status) VALUES (?,?,?)")) {
            preparedStatement.setObject(1, uniqueKey);
            preparedStatement.setObject(2, 1);
            preparedStatement.setObject(3, "OK");
            int actualCount = preparedStatement.executeUpdate();
            assertThat(actualCount, is(1));
        }
    }
    
    @Test
    public void assertMultiPrimaryKeyMigrationSuccess() throws Exception {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getContainerComposer().getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            consistencyCheckAlgorithmType = "CRC32_MATCH";
        } else {
            return;
        }
        KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        Object uniqueKey = keyGenerateAlgorithm.generateKey();
        assertMigrationSuccess(sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
            insertOneOrder(uniqueKey);
            getContainerComposer().assertProxyOrderRecordExist("t_order", uniqueKey);
            return null;
        });
    }
    
    @Test
    public void assertMultiUniqueKeyMigrationSuccess() throws Exception {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getContainerComposer().getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), UNIQUE KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            consistencyCheckAlgorithmType = "DATA_MATCH";
        } else {
            return;
        }
        KeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Object uniqueKey = keyGenerateAlgorithm.generateKey();
        assertMigrationSuccess(sql, "user_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
            insertOneOrder(uniqueKey);
            getContainerComposer().assertProxyOrderRecordExist("t_order", uniqueKey);
            return null;
        });
    }
    
    @Test
    public void assertSpecialTypeSingleColumnUniqueKeyMigrationSuccess() throws Exception {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getContainerComposer().getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARBINARY(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            // DATA_MATCH doesn't supported: Order by value must implements Comparable
            consistencyCheckAlgorithmType = "CRC32_MATCH";
        } else {
            return;
        }
        KeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        // TODO Insert binary string in VARBINARY column. But KeyGenerateAlgorithm.generateKey() require returning Comparable, and byte[] is not Comparable
        byte[] uniqueKey = new byte[]{-1, 0, 1};
        assertMigrationSuccess(sql, "order_id", keyGenerateAlgorithm, consistencyCheckAlgorithmType, () -> {
            insertOneOrder(uniqueKey);
            // TODO Select by byte[] from proxy doesn't work, so unhex function is used for now
            getContainerComposer().assertProxyOrderRecordExist(String.format("SELECT 1 FROM t_order WHERE order_id=UNHEX('%s')", Hex.encodeHexString(uniqueKey)));
            return null;
        });
    }
    
    private void assertMigrationSuccess(final String sqlPattern, final String shardingColumn, final KeyGenerateAlgorithm keyGenerateAlgorithm,
                                        final String consistencyCheckAlgorithmType, final Callable<Void> incrementalTaskFn) throws Exception {
        getContainerComposer().sourceExecuteWithLog(String.format(sqlPattern, SOURCE_TABLE_NAME));
        try (Connection connection = getContainerComposer().getSourceDataSource().getConnection()) {
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, SOURCE_TABLE_NAME, PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        }
        addMigrationProcessConfig();
        addMigrationSourceResource();
        addMigrationTargetResource();
        getContainerComposer().proxyExecuteWithLog(String.format(ORDER_TABLE_SHARDING_RULE_FORMAT, shardingColumn), 2);
        startMigration(SOURCE_TABLE_NAME, TARGET_TABLE_NAME);
        String jobId = listJobId().get(0);
        getContainerComposer().waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        incrementalTaskFn.call();
        getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        if (null != consistencyCheckAlgorithmType) {
            assertCheckMigrationSuccess(jobId, consistencyCheckAlgorithmType);
        }
        commitMigrationByJobId(jobId);
        getContainerComposer().proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertThat(getContainerComposer().getTargetTableRecordsCount(SOURCE_TABLE_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT + 1));
        List<String> lastJobIds = listJobId();
        assertTrue(lastJobIds.isEmpty());
    }
}
