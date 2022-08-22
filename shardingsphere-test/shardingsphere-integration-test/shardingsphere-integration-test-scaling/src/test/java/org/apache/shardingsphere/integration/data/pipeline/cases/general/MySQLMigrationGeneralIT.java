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

package org.apache.shardingsphere.integration.data.pipeline.cases.general;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseExtraSQLITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.task.MySQLIncrementTask;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ScalingITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingCaseHelper;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * General scaling test case, includes multiple cases.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class MySQLMigrationGeneralIT extends BaseExtraSQLITCase {
    
    private final ScalingParameterized parameterized;
    
    public MySQLMigrationGeneralIT(final ScalingParameterized parameterized) {
        super(parameterized);
        this.parameterized = parameterized;
        log.info("parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NONE) {
            return result;
        }
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String version : ENV.listDatabaseDockerImageNames(databaseType)) {
            result.add(new ScalingParameterized(databaseType, version, "env/scenario/general/mysql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertMigrationSuccess() {
        createScalingRule();
        createSourceOrderTable();
        createSourceOrderItemTable();
        addSourceResource();
        addTargetResource();
        createTargetOrderTableRule();
        createTargetOrderItemTableRule();
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getSourceDataSource());
        for (int i = 0; i < TABLE_INIT_ROW_COUNT / 1000; i++) {
            Pair<List<Object[]>, List<Object[]>> dataPair = ScalingCaseHelper.generateFullInsertData(keyGenerateAlgorithm, parameterized.getDatabaseType(), 1000);
            jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrder(), dataPair.getLeft());
            jdbcTemplate.batchUpdate(getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        }
        startMigrationOrderItem();
        checkOrderMigration(keyGenerateAlgorithm, jdbcTemplate);
        checkOrderItemMigration();
        for (String each : listJobId()) {
            cleanMigrationByJobId(each);
        }
        List<String> lastJobIds = listJobId();
        assertThat(lastJobIds.size(), is(0));
        assertGreaterThanOrderTableInitRows(TABLE_INIT_ROW_COUNT, "");
    }
    
    private void checkOrderMigration(final KeyGenerateAlgorithm keyGenerateAlgorithm, final JdbcTemplate jdbcTemplate) {
        startMigrationOrder();
        startIncrementTask(new MySQLIncrementTask(jdbcTemplate, keyGenerateAlgorithm, true, 20));
        String jobId = getJobIdByTableName("t_order");
        waitMigrationFinished(jobId);
        assertCheckScalingSuccess(jobId);
        stopMigrationByJobId(jobId);
    }
    
    private void checkOrderItemMigration() {
        startMigrationOrderItem();
        String jobId = getJobIdByTableName("t_order_item");
        waitMigrationFinished(jobId);
        assertCheckScalingSuccess(jobId);
        stopMigrationByJobId(jobId);
    }
}
