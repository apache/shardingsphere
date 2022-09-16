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
import org.apache.shardingsphere.integration.data.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * PostgreSQL general scaling test case. include openGauss type, same process.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class PostgreSQLMigrationGeneralIT extends AbstractMigrationITCase {
    
    private static final KeyGenerateAlgorithm KEY_GENERATE_ALGORITHM = new AutoIncrementKeyGenerateAlgorithm();
    
    private final ScalingParameterized parameterized;
    
    public PostgreSQLMigrationGeneralIT(final ScalingParameterized parameterized) {
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
        Pair<List<Object[]>, List<Object[]>> dataPair = ScalingCaseHelper.generateFullInsertData(KEY_GENERATE_ALGORITHM, parameterized.getDatabaseType(), TABLE_INIT_ROW_COUNT);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getSourceDataSource());
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrder(), dataPair.getLeft());
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        checkOrderMigration(jdbcTemplate);
        checkOrderItemMigration();
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            for (String each : listJobId()) {
                commitMigrationByJobId(each);
            }
            List<String> lastJobIds = listJobId();
            assertThat(lastJobIds.size(), is(0));
        }
        assertGreaterThanOrderTableInitRows(TABLE_INIT_ROW_COUNT, SCHEMA_NAME);
    }
    
    private void checkOrderMigration(final JdbcTemplate jdbcTemplate) throws SQLException, InterruptedException {
        startMigrationOrderCopy(true);
        startIncrementTask(new PostgreSQLIncrementTask(jdbcTemplate, SCHEMA_NAME, false, 20));
        String jobId = getJobIdByTableName("t_order_copy");
        waitJobFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        stopMigrationByJobId(jobId);
        sourceExecuteWithLog(String.format("INSERT INTO %s.t_order_copy (order_id,user_id,status) VALUES (%s, %s, '%s')", SCHEMA_NAME, KEY_GENERATE_ALGORITHM.generateKey(),
                1, "afterStop"));
        startMigrationByJobId(jobId);
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        stopMigrationByJobId(jobId);
    }
    
    private void checkOrderItemMigration() throws SQLException, InterruptedException {
        startMigrationOrderItem(true);
        String jobId = getJobIdByTableName("t_order_item");
        waitJobFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        stopMigrationByJobId(jobId);
    }
}
