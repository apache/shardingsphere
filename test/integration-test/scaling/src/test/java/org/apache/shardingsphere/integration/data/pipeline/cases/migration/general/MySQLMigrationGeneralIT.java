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

package org.apache.shardingsphere.integration.data.pipeline.cases.migration.general;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.migration.AbstractMigrationITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.task.MySQLIncrementTask;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingCaseHelper;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * General migration test case, includes multiple cases.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class MySQLMigrationGeneralIT extends AbstractMigrationITCase {
    
    private final ScalingParameterized parameterized;
    
    public MySQLMigrationGeneralIT(final ScalingParameterized parameterized) {
        super(parameterized);
        this.parameterized = parameterized;
        log.info("parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ITEnvTypeEnum.NONE) {
            return result;
        }
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String each : ENV.listStorageContainerImages(databaseType)) {
            result.add(new ScalingParameterized(databaseType, each, "env/scenario/general/mysql.xml"));
        }
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order_copy";
    }
    
    @Test
    public void assertMigrationSuccess() throws SQLException, InterruptedException {
        addMigrationProcessConfig();
        createSourceOrderTable();
        createSourceOrderItemTable();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        createTargetOrderTableEncryptRule();
        createTargetOrderItemTableRule();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getSourceDataSource());
        Pair<List<Object[]>, List<Object[]>> dataPair = ScalingCaseHelper.generateFullInsertData(parameterized.getDatabaseType(), 3000);
        log.info("init data begin: {}", LocalDateTime.now());
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrder(getSourceTableOrderName()), dataPair.getLeft());
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        log.info("init data end: {}", LocalDateTime.now());
        startMigration(getSourceTableOrderName(), getTargetTableOrderName());
        startMigration("t_order_item", "t_order_item");
        String orderJobId = getJobIdByTableName(getSourceTableOrderName());
        waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", orderJobId));
        startIncrementTask(new MySQLIncrementTask(jdbcTemplate, getSourceTableOrderName(), new SnowflakeKeyGenerateAlgorithm(), 30));
        assertMigrationSuccessById(orderJobId, "DATA_MATCH");
        String orderItemJobId = getJobIdByTableName("t_order_item");
        assertMigrationSuccessById(orderItemJobId, "DATA_MATCH");
        assertMigrationSuccessById(orderItemJobId, "CRC32_MATCH");
        for (String each : listJobId()) {
            commitMigrationByJobId(each);
        }
        List<String> lastJobIds = listJobId();
        assertThat(lastJobIds.size(), is(0));
        assertGreaterThanOrderTableInitRows(TABLE_INIT_ROW_COUNT, "");
    }
    
    private void assertMigrationSuccessById(final String jobId, final String algorithmType) throws SQLException, InterruptedException {
        List<Map<String, Object>> jobStatus = waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        for (Map<String, Object> each : jobStatus) {
            assertTrue(Integer.parseInt(each.get("processed_records_count").toString()) > 0);
        }
        proxyExecuteWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='%s')", jobId, algorithmType), 2);
        proxyExecuteWithLog(String.format("STOP MIGRATION CHECK '%s'", jobId), 2);
        proxyExecuteWithLog(String.format("START MIGRATION CHECK '%s'", jobId), 0);
        assertMigrationCheckSuccess(jobId);
    }
}
