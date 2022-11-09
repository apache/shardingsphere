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
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.migration.AbstractMigrationITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.task.PostgreSQLIncrementTask;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * PostgreSQL and openGauss general scaling test case.
 */
@RunWith(Parameterized.class)
@Slf4j
public final class PostgreSQLMigrationGeneralIT extends AbstractMigrationITCase {
    
    private final ScalingParameterized parameterized;
    
    public PostgreSQLMigrationGeneralIT(final ScalingParameterized parameterized) {
        super(parameterized);
        this.parameterized = parameterized;
        log.info("parameterized:{}", parameterized);
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order_copy";
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ITEnvTypeEnum.NONE) {
            return result;
        }
        for (String each : ENV.listStorageContainerImages(new PostgreSQLDatabaseType())) {
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), each, "env/scenario/general/postgresql.xml"));
        }
        for (String each : ENV.listStorageContainerImages(new OpenGaussDatabaseType())) {
            result.add(new ScalingParameterized(new OpenGaussDatabaseType(), each, "env/scenario/general/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertMigrationSuccess() throws SQLException, InterruptedException {
        addMigrationProcessConfig();
        createSourceSchema(SCHEMA_NAME);
        createSourceOrderTable();
        createSourceOrderItemTable();
        createSourceTableIndexList(SCHEMA_NAME);
        createSourceCommentOnList(SCHEMA_NAME);
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        createTargetOrderItemTableRule();
        Pair<List<Object[]>, List<Object[]>> dataPair = ScalingCaseHelper.generateFullInsertData(parameterized.getDatabaseType(), TABLE_INIT_ROW_COUNT);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getSourceDataSource());
        log.info("init data begin: {}", LocalDateTime.now());
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrder(getSourceTableOrderName()), dataPair.getLeft());
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        log.info("init data end: {}", LocalDateTime.now());
        checkOrderMigration(jdbcTemplate);
        checkOrderItemMigration();
        for (String each : listJobId()) {
            commitMigrationByJobId(each);
        }
        List<String> lastJobIds = listJobId();
        assertThat(lastJobIds.size(), is(0));
        assertGreaterThanOrderTableInitRows(TABLE_INIT_ROW_COUNT, SCHEMA_NAME);
    }
    
    private void checkOrderMigration(final JdbcTemplate jdbcTemplate) throws SQLException, InterruptedException {
        startMigrationWithSchema(getSourceTableOrderName(), "t_order");
        startIncrementTask(new PostgreSQLIncrementTask(jdbcTemplate, SCHEMA_NAME, getSourceTableOrderName(), 20));
        String jobId = getJobIdByTableName(getSourceTableOrderName());
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        stopMigrationByJobId(jobId);
        long recordId = new SnowflakeKeyGenerateAlgorithm().generateKey();
        sourceExecuteWithLog(String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')", String.join(".", SCHEMA_NAME, getSourceTableOrderName()), recordId, 1, "afterStop"));
        startMigrationByJobId(jobId);
        // must refresh firstly, otherwise proxy can't get schema and table info
        proxyExecuteWithLog("REFRESH TABLE METADATA;", 2);
        assertProxyOrderRecordExist(recordId, String.join(".", SCHEMA_NAME, getTargetTableOrderName()));
        proxyExecuteWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='%s')", jobId, "DATA_MATCH"), 2);
        assertMigrationCheckSuccess(jobId);
    }
    
    private void checkOrderItemMigration() throws SQLException, InterruptedException {
        startMigrationWithSchema("t_order_item", "t_order_item");
        String jobId = getJobIdByTableName("t_order_item");
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        proxyExecuteWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='%s')", jobId, "DATA_MATCH"), 2);
        assertMigrationCheckSuccess(jobId);
    }
}
