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
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

/**
 * E2E IT for different types of indexes, includes:
 * 1) no unique key.
 * 2) special type single column unique key.
 */
@RunWith(Parameterized.class)
@Slf4j
public final class IndexesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private final PipelineTestParameter testParam;
    
    public IndexesMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
        this.testParam = testParam;
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        for (String version : PipelineBaseE2EIT.ENV.listStorageContainerImages(new MySQLDatabaseType())) {
            result.add(new PipelineTestParameter(new MySQLDatabaseType(), version, "env/common/none.xml"));
        }
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order";
    }
    
    @Test
    public void assertNoUniqueKeyMigrationSuccess() throws SQLException, InterruptedException {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            // DATA_MATCH doesn't supported, could not order by records
            consistencyCheckAlgorithmType = "CRC32_MATCH";
        } else {
            return;
        }
        assertMigrationSuccess(sql, consistencyCheckAlgorithmType);
    }
    
    @Test
    public void assertMultiPrimaryKeyMigrationSuccess() throws SQLException, InterruptedException {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            consistencyCheckAlgorithmType = "CRC32_MATCH";
        } else {
            return;
        }
        assertMigrationSuccess(sql, consistencyCheckAlgorithmType);
    }
    
    @Test
    public void assertMultiUniqueKeyMigrationSuccess() throws SQLException, InterruptedException {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARCHAR(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), UNIQUE KEY (`order_id`,`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            consistencyCheckAlgorithmType = "DATA_MATCH";
        } else {
            return;
        }
        assertMigrationSuccess(sql, consistencyCheckAlgorithmType);
    }
    
    @Test
    public void assertSpecialTypeSingleColumnUniqueKeyMigrationSuccess() throws SQLException, InterruptedException {
        String sql;
        String consistencyCheckAlgorithmType;
        if (getDatabaseType() instanceof MySQLDatabaseType) {
            sql = "CREATE TABLE `%s` (`order_id` VARBINARY(64) NOT NULL, `user_id` INT NOT NULL, `status` varchar(255), PRIMARY KEY (`order_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            // DATA_MATCH doesn't supported: Order by value must implements Comparable
            consistencyCheckAlgorithmType = "CRC32_MATCH";
        } else {
            return;
        }
        assertMigrationSuccess(sql, consistencyCheckAlgorithmType);
    }
    
    private void assertMigrationSuccess(final String sqlPattern, final String consistencyCheckAlgorithmType) throws SQLException, InterruptedException {
        initEnvironment(testParam.getDatabaseType(), new MigrationJobType());
        createSourceOrderTable(sqlPattern);
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
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, consistencyCheckAlgorithmType);
        commitMigrationByJobId(jobId);
        proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertTargetAndSourceCountAreSame();
        List<String> lastJobIds = listJobId();
        assertThat(lastJobIds.size(), is(0));
    }
    
    private void createSourceOrderTable(final String sqlPattern) throws SQLException {
        sourceExecuteWithLog(String.format(sqlPattern, getSourceTableOrderName()));
    }
    
    private void assertTargetAndSourceCountAreSame() {
        List<Map<String, Object>> targetList = queryForListWithLog("SELECT COUNT(*) AS count FROM t_order");
        assertFalse(targetList.isEmpty());
        assertThat(Integer.parseInt(targetList.get(0).get("count").toString()), is(PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT));
    }
}
